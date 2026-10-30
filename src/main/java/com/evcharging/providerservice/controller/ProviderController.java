package com.evcharging.providerservice.controller;

import com.evcharging.providerservice.dto.LoginRequest;
import com.evcharging.providerservice.dto.ProviderDTO;
import com.evcharging.providerservice.security.JwtTokenProvider;
import com.evcharging.providerservice.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.evcharging.providerservice.dto.MfaCodeRequest;
import com.evcharging.providerservice.dto.MfaLoginRequest;
import com.evcharging.providerservice.mfa.MfaService;
import com.evcharging.providerservice.repository.ProviderRepository;
import com.evcharging.providerservice.entity.Provider;
import javax.validation.Valid;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private MfaService mfaService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProvider(@Valid @RequestBody ProviderDTO providerDTO) {
        try {
            ProviderDTO registeredProvider = providerService.registerProvider(providerDTO);
            String token = jwtTokenProvider.generateToken(registeredProvider.getEmail(), registeredProvider.getProviderId(), "PROVIDER");
            String refreshToken = jwtTokenProvider.generateRefreshToken(registeredProvider.getEmail(), registeredProvider.getProviderId(), "PROVIDER");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Provider registered successfully");
            response.put("provider", registeredProvider);
            response.put("token", token);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            ProviderDTO provider = providerService.login(loginRequest.getEmail(), loginRequest.getPassword());
            if (Boolean.TRUE.equals(provider.getMfaEnabled())) {
                return ResponseEntity.ok(Map.of(
                        "mfaRequired", true,
                        "mfaToken", jwtTokenProvider.generateMfaChallengeToken(provider.getEmail(), provider.getProviderId(), "PROVIDER")));
            }
            String token = jwtTokenProvider.generateToken(provider.getEmail(), provider.getProviderId(), "PROVIDER");
            String refreshToken = jwtTokenProvider.generateRefreshToken(provider.getEmail(), provider.getProviderId(), "PROVIDER");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("provider", provider);
            response.put("token", token);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login/mfa")
    public ResponseEntity<?> loginWithMfa(@Valid @RequestBody MfaLoginRequest request) {
        Long id;
        try {
            id = jwtTokenProvider.parseMfaChallenge(request.getMfaToken(), "PROVIDER");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Your sign-in session expired. Please sign in again."));
        }
        Provider account = providerRepository.findById(id).orElse(null);
        if (account == null || !Boolean.TRUE.equals(account.getIsActive()) || !mfaService.isEnabled(account)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sign-in failed"));
        }
        MfaService.Result result = mfaService.verify(account, request.getCode());
        providerRepository.save(account);
        if (result == MfaService.Result.LOCKED) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many incorrect codes. Try again in 15 minutes."));
        }
        if (result != MfaService.Result.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid verification code"));
        }
        String token = jwtTokenProvider.generateToken(account.getEmail(), account.getProviderId(), "PROVIDER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(account.getEmail(), account.getProviderId(), "PROVIDER");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("provider", providerService.getProviderById(id));
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        return ResponseEntity.ok(response);
    }

    /** Step 1 of enrolling an authenticator app: returns the secret and otpauth:// URI to show as a QR code. */
    @PostMapping("/{providerId}/mfa/setup")
    public ResponseEntity<?> mfaSetup(@PathVariable Long providerId) {
        if (!isSelf(providerId)) {
            return mfaForbidden();
        }
        try {
            Provider account = providerRepository.findById(providerId).orElseThrow(() -> new RuntimeException("Account not found"));
            MfaService.Setup setup = mfaService.beginSetup(account);
            providerRepository.save(account);
            return ResponseEntity.ok(Map.of("secret", setup.secret(), "otpAuthUri", setup.otpAuthUri()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /** Step 2: confirm with a code from the app. Returns the one-time recovery codes -- shown only once. */
    @PostMapping("/{providerId}/mfa/enable")
    public ResponseEntity<?> mfaEnable(@PathVariable Long providerId, @Valid @RequestBody MfaCodeRequest request) {
        if (!isSelf(providerId)) {
            return mfaForbidden();
        }
        try {
            Provider account = providerRepository.findById(providerId).orElseThrow(() -> new RuntimeException("Account not found"));
            List<String> recoveryCodes = mfaService.enable(account, request.getCode().replaceAll("\\s", ""));
            providerRepository.save(account);
            return ResponseEntity.ok(Map.of("recoveryCodes", recoveryCodes));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /** Turning MFA off needs a valid authenticator or recovery code, so a stolen session alone can't do it. */
    @PostMapping("/{providerId}/mfa/disable")
    public ResponseEntity<?> mfaDisable(@PathVariable Long providerId, @Valid @RequestBody MfaCodeRequest request) {
        if (!isSelf(providerId)) {
            return mfaForbidden();
        }
        Provider account = providerRepository.findById(providerId).orElse(null);
        if (account == null || !mfaService.isEnabled(account)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Two-factor authentication is not enabled"));
        }
        MfaService.Result result = mfaService.verify(account, request.getCode());
        if (result == MfaService.Result.OK) {
            mfaService.disable(account);
        }
        providerRepository.save(account);
        if (result == MfaService.Result.LOCKED) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Too many incorrect codes. Try again in 15 minutes."));
        }
        if (result != MfaService.Result.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid verification code"));
        }
        return ResponseEntity.ok(Map.of("message", "Two-factor authentication turned off"));
    }

    private ResponseEntity<?> mfaForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "You can only manage two-factor authentication for your own account"));
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<?> getProviderById(@PathVariable Long providerId) {
        try {
            ProviderDTO provider = providerService.getProviderById(providerId);
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getProviderByEmail(@PathVariable String email) {
        try {
            ProviderDTO provider = providerService.getProviderByEmail(email);
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProviders() {
        try {
            return ResponseEntity.ok(providerService.getAllActiveProviders());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<?> getProvidersByCity(@PathVariable String city) {
        try {
            return ResponseEntity.ok(providerService.getProvidersByCity(city));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{providerId}")
    public ResponseEntity<?> updateProvider(@PathVariable Long providerId,
                                           @Valid @RequestBody ProviderDTO providerDTO) {
        if (!isSelf(providerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only modify your own station"));
        }
        try {
            ProviderDTO updatedProvider = providerService.updateProvider(providerId, providerDTO);
            return ResponseEntity.ok(updatedProvider);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{providerId}")
    public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
        if (!isSelf(providerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only delete your own station"));
        }
        try {
            providerService.deleteProvider(providerId);
            return ResponseEntity.ok(Map.of("message", "Provider deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/exists/{providerId}")
    public ResponseEntity<?> providerExists(@PathVariable Long providerId) {
        boolean exists = providerService.providerExists(providerId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /** True only when the caller's own JWT identifies them as this exact provider. */
    private boolean isSelf(Long providerId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return false;
        }
        boolean isProviderRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"));
        return isProviderRole && providerId.equals(auth.getPrincipal());
    }
}
