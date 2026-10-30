package com.evcharging.providerservice.service;

import com.evcharging.providerservice.dto.ProviderDTO;
import com.evcharging.providerservice.entity.Provider;
import com.evcharging.providerservice.repository.ProviderRepository;
import com.evcharging.providerservice.security.PiiCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StationService stationService;

    @Autowired
    private PiiCipher piiCipher;

    private static final int MAX_PICTURE_CHARS = 400_000;

    public ProviderDTO registerProvider(ProviderDTO providerDTO) {
        if (providerRepository.findByEmail(providerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Provider with email already exists");
        }
        if (providerDTO.getPassword() == null || providerDTO.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (providerDTO.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        Provider provider = new Provider();
        provider.setCompanyName(providerDTO.getCompanyName());
        provider.setContactPersonName(providerDTO.getContactPersonName());
        provider.setEmail(providerDTO.getEmail());
        provider.setPhoneNumber(providerDTO.getPhoneNumber());
        provider.setPasswordHash(passwordEncoder.encode(providerDTO.getPassword()));
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
        stationService.createDefaultStation(savedProvider);
        return convertToFullDTO(savedProvider);
    }

    /** Throws the same generic error for "no such account" and "wrong password" to avoid account enumeration. */
    public ProviderDTO login(String email, String rawPassword) {
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (provider.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, provider.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(provider.getIsActive())) {
            throw new RuntimeException("This account has been deactivated");
        }
        return convertToFullDTO(provider);
    }

    public ProviderDTO getProviderById(Long providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        return convertToFullDTO(provider);
    }

    public ProviderDTO getProviderByEmail(String email) {
        Provider provider = providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Provider not found with email: " + email));
        return convertToFullDTO(provider);
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
        applyIdentity(provider, providerDTO);
        applyPicture(provider, providerDTO.getProfilePicture());

        Provider updatedProvider = providerRepository.save(provider);
        return convertToFullDTO(updatedProvider);
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

    /** Stores a newly supplied PAN/Aadhaar (blank = keep what's on file) and requires at least one to exist. */
    private void applyIdentity(Provider provider, ProviderDTO dto) {
        if (dto.getPanNumber() != null && !dto.getPanNumber().isBlank()) {
            provider.setPanNumberEnc(piiCipher.encrypt(IdentityRules.normalisePan(dto.getPanNumber())));
        }
        if (dto.getAadhaarNumber() != null && !dto.getAadhaarNumber().isBlank()) {
            provider.setAadhaarNumberEnc(piiCipher.encrypt(IdentityRules.normaliseAadhaar(dto.getAadhaarNumber())));
        }
        if (provider.getPanNumberEnc() == null && provider.getAadhaarNumberEnc() == null) {
            throw new IllegalArgumentException("Provide your PAN or Aadhaar number");
        }
    }

    private void applyPicture(Provider provider, String picture) {
        if (picture == null) {
            return; // not supplied -- keep the current photo
        }
        if (picture.isEmpty()) {
            provider.setProfilePicture(null);
            return;
        }
        boolean imageUrl = picture.startsWith("data:image/jpeg;base64,") || picture.startsWith("data:image/png;base64,")
                || picture.startsWith("data:image/webp;base64,");
        if (!imageUrl) {
            throw new IllegalArgumentException("The profile picture must be a JPEG, PNG or WebP image");
        }
        if (picture.length() > MAX_PICTURE_CHARS) {
            throw new IllegalArgumentException("The profile picture is too large");
        }
        provider.setProfilePicture(picture);
    }

    private ProviderDTO convertToFullDTO(Provider provider) {
        ProviderDTO dto = convertToDTO(provider);
        dto.setProfilePicture(provider.getProfilePicture());
        if (provider.getPanNumberEnc() != null) {
            dto.setPanMasked(IdentityRules.maskPan(piiCipher.decrypt(provider.getPanNumberEnc())));
        }
        if (provider.getAadhaarNumberEnc() != null) {
            dto.setAadhaarMasked(IdentityRules.maskAadhaar(piiCipher.decrypt(provider.getAadhaarNumberEnc())));
        }
        return dto;
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
        dto.setMfaEnabled(Boolean.TRUE.equals(provider.getMfaEnabled()));
        dto.setCreatedAt(provider.getCreatedAt());
        dto.setUpdatedAt(provider.getUpdatedAt());
        return dto;
    }
}
