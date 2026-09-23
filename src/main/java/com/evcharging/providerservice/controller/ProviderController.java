package com.evcharging.providerservice.controller;

import com.evcharging.providerservice.dto.ProviderDTO;
import com.evcharging.providerservice.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProvider(@Valid @RequestBody ProviderDTO providerDTO) {
        try {
            ProviderDTO registeredProvider = providerService.registerProvider(providerDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Provider registered successfully");
            response.put("provider", registeredProvider);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
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
}
