// ============= PROVIDER ENTITY =============
package com.evcharging.providerservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "providers", indexes = {
    @Index(name = "idx_providers_email", columnList = "email"),
    @Index(name = "idx_providers_city", columnList = "city"),
    @Index(name = "idx_providers_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;

    @NotBlank(message = "Company name is required")
    @Column(nullable = false)
    private String companyName;

    @NotBlank(message = "Contact person name is required")
    @Column(nullable = false)
    private String contactPersonName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(length = 500, nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double ratePerHour;

    @Column
    private String chargerType;

    @Column
    private Integer maxConcurrentCharges;

    @Column
    private String oauthProvider;

    @Column
    private String oauthId;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

// ============= PROVIDER REPOSITORY =============
package com.evcharging.providerservice.repository;

import com.evcharging.providerservice.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByEmail(String email);
    Optional<Provider> findByPhoneNumber(String phoneNumber);
    Optional<Provider> findByOauthId(String oauthId);
    List<Provider> findByCity(String city);
    List<Provider> findByIsActiveTrue();
}

// ============= PROVIDER DTO =============
package com.evcharging.providerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDTO {
    private Long providerId;
    @NotBlank(message = "Company name is required")
    private String companyName;
    @NotBlank(message = "Contact person name is required")
    private String contactPersonName;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Address is required")
    private String address;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "State is required")
    private String state;
    @NotBlank(message = "Zip code is required")
    private String zipCode;
    @NotNull(message = "Latitude is required")
    private Double latitude;
    @NotNull(message = "Longitude is required")
    private Double longitude;
    @NotNull(message = "Rate per hour is required")
    private Double ratePerHour;
    private String chargerType;
    private Integer maxConcurrentCharges;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// ============= PROVIDER SERVICE =============
package com.evcharging.providerservice.service;

import com.evcharging.providerservice.dto.ProviderDTO;
import com.evcharging.providerservice.entity.Provider;
import com.evcharging.providerservice.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    public ProviderDTO registerProvider(ProviderDTO providerDTO) {
        if (providerRepository.findByEmail(providerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Provider with email already exists");
        }

        Provider provider = new Provider();
        provider.setCompanyName(providerDTO.getCompanyName());
        provider.setContactPersonName(providerDTO.getContactPersonName());
        provider.setEmail(providerDTO.getEmail());
        provider.setPhoneNumber(providerDTO.getPhoneNumber());
        provider.setAddress(providerDTO.getAddress());
        provider.setCity(providerDTO.getCity());
        provider.setState(providerDTO.getState());
        provider.setZipCode(providerDTO.getZipCode());
        provider.setLatitude(providerDTO.getLatitude());
        provider.setLongitude(providerDTO.getLongitude());
        provider.setRatePerHour(providerDTO.getRatePerHour());
        provider.setChargerType(providerDTO.getChargerType());
        provider.setMaxConcurrentCharges(providerDTO.getMaxConcurrentCharges());
        provider.setIsActive(true);

        Provider savedProvider = providerRepository.save(provider);
        return convertToDTO(savedProvider);
    }

    public ProviderDTO getProviderById(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        return convertToDTO(provider);
    }

    public ProviderDTO getProviderByEmail(String email) {
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Provider not found with email: " + email));
        return convertToDTO(provider);
    }

    public List<ProviderDTO> getAllActiveProviders() {
        return providerRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProviderDTO> getProvidersByCity(String city) {
        return providerRepository.findByCity(city)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProviderDTO updateProvider(Long providerId, ProviderDTO providerDTO) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));

        provider.setCompanyName(providerDTO.getCompanyName());
        provider.setContactPersonName(providerDTO.getContactPersonName());
        provider.setPhoneNumber(providerDTO.getPhoneNumber());
        provider.setAddress(providerDTO.getAddress());
        provider.setCity(providerDTO.getCity());
        provider.setState(providerDTO.getState());
        provider.setZipCode(providerDTO.getZipCode());
        provider.setLatitude(providerDTO.getLatitude());
        provider.setLongitude(providerDTO.getLongitude());
        provider.setRatePerHour(providerDTO.getRatePerHour());
        provider.setChargerType(providerDTO.getChargerType());
        provider.setMaxConcurrentCharges(providerDTO.getMaxConcurrentCharges());

        Provider updatedProvider = providerRepository.save(provider);
        return convertToDTO(updatedProvider);
    }

    public void deleteProvider(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        provider.setIsActive(false);
        providerRepository.save(provider);
    }

    public boolean providerExists(Long providerId) {
        return providerRepository.existsById(providerId);
    }

    private ProviderDTO convertToDTO(Provider provider) {
        ProviderDTO dto = new ProviderDTO();
        dto.setProviderId(provider.getProviderId());
        dto.setCompanyName(provider.getCompanyName());
        dto.setContactPersonName(provider.getContactPersonName());
        dto.setEmail(provider.getEmail());
        dto.setPhoneNumber(provider.getPhoneNumber());
        dto.setAddress(provider.getAddress());
        dto.setCity(provider.getCity());
        dto.setState(provider.getState());
        dto.setZipCode(provider.getZipCode());
        dto.setLatitude(provider.getLatitude());
        dto.setLongitude(provider.getLongitude());
        dto.setRatePerHour(provider.getRatePerHour());
        dto.setChargerType(provider.getChargerType());
        dto.setMaxConcurrentCharges(provider.getMaxConcurrentCharges());
        dto.setCreatedAt(provider.getCreatedAt());
        dto.setUpdatedAt(provider.getUpdatedAt());
        return dto;
    }
}

// ============= PROVIDER CONTROLLER =============
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
