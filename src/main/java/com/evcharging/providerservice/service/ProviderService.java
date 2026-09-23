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
