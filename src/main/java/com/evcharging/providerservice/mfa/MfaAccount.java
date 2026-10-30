package com.evcharging.providerservice.mfa;

import java.time.LocalDateTime;

/** The MFA state an account entity carries; implemented by the entity (Lombok supplies the accessors). */
public interface MfaAccount {
    String getEmail();

    Boolean getMfaEnabled();
    void setMfaEnabled(Boolean enabled);

    /** AES-GCM encrypted base32 TOTP secret (see {@link MfaSecretCipher}). */
    String getMfaSecret();
    void setMfaSecret(String secret);

    /** Last accepted TOTP time step, so a code can't be replayed. */
    Long getMfaLastStep();
    void setMfaLastStep(Long step);

    /** Comma-separated SHA-256 hashes of the unused one-time recovery codes. */
    String getMfaRecoveryCodes();
    void setMfaRecoveryCodes(String codes);

    Integer getMfaFailedAttempts();
    void setMfaFailedAttempts(Integer attempts);

    LocalDateTime getMfaLockedUntil();
    void setMfaLockedUntil(LocalDateTime until);
}
