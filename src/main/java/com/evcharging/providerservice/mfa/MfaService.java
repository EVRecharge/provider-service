package com.evcharging.providerservice.mfa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TOTP enrollment and verification for any {@link MfaAccount}. It only mutates the account -- the
 * caller must save it afterwards, including after a failed attempt, so the lockout counter sticks.
 */
@Service
public class MfaService {

    public enum Result { OK, INVALID, LOCKED }

    public static final class Setup {
        private final String secret;
        private final String otpAuthUri;

        public Setup(String secret, String otpAuthUri) {
            this.secret = secret;
            this.otpAuthUri = otpAuthUri;
        }

        public String secret() { return secret; }
        public String otpAuthUri() { return otpAuthUri; }
    }

    private static final String ISSUER = "EVRecharge India";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;
    private static final int RECOVERY_CODE_COUNT = 8;
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Autowired
    private TotpService totp;

    @Autowired
    private MfaSecretCipher cipher;

    private final SecureRandom random = new SecureRandom();

    public boolean isEnabled(MfaAccount account) {
        return Boolean.TRUE.equals(account.getMfaEnabled());
    }

    /** Starts (or restarts) enrollment: stores a fresh pending secret that isn't active until {@link #enable}. */
    public Setup beginSetup(MfaAccount account) {
        if (isEnabled(account)) {
            throw new IllegalStateException("Two-factor authentication is already enabled");
        }
        String secret = totp.generateSecret();
        account.setMfaSecret(cipher.encrypt(secret));
        return new Setup(secret, totp.otpAuthUri(ISSUER, account.getEmail(), secret));
    }

    /** Confirms the authenticator is set up correctly, turns MFA on and returns the one-time recovery codes. */
    public List<String> enable(MfaAccount account, String code) {
        if (isEnabled(account)) {
            throw new IllegalStateException("Two-factor authentication is already enabled");
        }
        if (account.getMfaSecret() == null) {
            throw new IllegalStateException("Start setup first");
        }
        long step = totp.verify(cipher.decrypt(account.getMfaSecret()), code, System.currentTimeMillis());
        if (step < 0) {
            throw new IllegalArgumentException("That code didn't match. Check your authenticator app and try again.");
        }
        List<String> recovery = newRecoveryCodes();
        account.setMfaEnabled(true);
        account.setMfaLastStep(step);
        account.setMfaRecoveryCodes(String.join(",", recovery.stream().map(this::hash).collect(Collectors.toList())));
        resetFailures(account);
        return recovery;
    }

    /** Checks an authenticator code or an unused recovery code. */
    public Result verify(MfaAccount account, String code) {
        LocalDateTime now = LocalDateTime.now();
        if (account.getMfaLockedUntil() != null && account.getMfaLockedUntil().isAfter(now)) {
            return Result.LOCKED;
        }
        if (isEnabled(account) && account.getMfaSecret() != null && code != null) {
            String trimmed = code.replaceAll("\\s", "");
            long step = totp.verify(cipher.decrypt(account.getMfaSecret()), trimmed, System.currentTimeMillis());
            long last = account.getMfaLastStep() == null ? -1 : account.getMfaLastStep();
            if (step >= 0 && step > last) {
                account.setMfaLastStep(step);
                resetFailures(account);
                return Result.OK;
            }
            if (step < 0 && consumeRecoveryCode(account, trimmed)) {
                resetFailures(account);
                return Result.OK;
            }
        }
        int failed = (account.getMfaFailedAttempts() == null ? 0 : account.getMfaFailedAttempts()) + 1;
        if (failed >= MAX_FAILED_ATTEMPTS) {
            account.setMfaLockedUntil(now.plusMinutes(LOCK_MINUTES));
            failed = 0;
        }
        account.setMfaFailedAttempts(failed);
        return Result.INVALID;
    }

    public void disable(MfaAccount account) {
        account.setMfaEnabled(false);
        account.setMfaSecret(null);
        account.setMfaLastStep(null);
        account.setMfaRecoveryCodes(null);
        resetFailures(account);
    }

    private boolean consumeRecoveryCode(MfaAccount account, String code) {
        if (account.getMfaRecoveryCodes() == null || account.getMfaRecoveryCodes().isBlank()) {
            return false;
        }
        String hashed = hash(code.toUpperCase().replace("-", ""));
        List<String> remaining = new ArrayList<>(Arrays.asList(account.getMfaRecoveryCodes().split(",")));
        if (!remaining.remove(hashed)) {
            return false;
        }
        account.setMfaRecoveryCodes(String.join(",", remaining));
        return true;
    }

    private void resetFailures(MfaAccount account) {
        account.setMfaFailedAttempts(0);
        account.setMfaLockedUntil(null);
    }

    private List<String> newRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                sb.append(RECOVERY_ALPHABET.charAt(random.nextInt(RECOVERY_ALPHABET.length())));
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    private String hash(String code) {
        try {
            StringBuilder hex = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8))) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
