package com.evcharging.providerservice.controller;

import com.evcharging.providerservice.dto.StationDTO;
import com.evcharging.providerservice.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StationController {

    @Autowired
    private StationService stationService;

    /** Every active station -- what drivers search over. */
    @GetMapping("/stations")
    public ResponseEntity<?> listActive() {
        return ResponseEntity.ok(stationService.listActive());
    }

    @GetMapping("/stations/{stationId}")
    public ResponseEntity<?> get(@PathVariable Long stationId) {
        try {
            return ResponseEntity.ok(stationService.get(stationId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/providers/{providerId}/stations")
    public ResponseEntity<?> listForProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(stationService.listForProvider(providerId));
    }

    @PostMapping("/providers/{providerId}/stations")
    public ResponseEntity<?> create(@PathVariable Long providerId, @Valid @RequestBody StationDTO dto) {
        if (!isProvider(providerId)) {
            return forbidden("You can only add stations to your own account");
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(stationService.create(providerId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/stations/{stationId}")
    public ResponseEntity<?> update(@PathVariable Long stationId, @Valid @RequestBody StationDTO dto) {
        try {
            if (!isProvider(stationService.ownerOf(stationId))) {
                return forbidden("You can only modify your own stations");
            }
            return ResponseEntity.ok(stationService.update(stationId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/stations/{stationId}")
    public ResponseEntity<?> deactivate(@PathVariable Long stationId) {
        try {
            if (!isProvider(stationService.ownerOf(stationId))) {
                return forbidden("You can only remove your own stations");
            }
            stationService.deactivate(stationId);
            return ResponseEntity.ok(Map.of("message", "Station removed"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<?> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", message));
    }

    /** True only when the caller's own JWT identifies them as this exact provider account. */
    private boolean isProvider(Long providerId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            return false;
        }
        boolean isProviderRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROVIDER"));
        return isProviderRole && providerId.equals(auth.getPrincipal());
    }
}
