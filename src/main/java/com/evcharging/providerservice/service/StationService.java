package com.evcharging.providerservice.service;

import com.evcharging.providerservice.dto.StationDTO;
import com.evcharging.providerservice.entity.Provider;
import com.evcharging.providerservice.entity.Station;
import com.evcharging.providerservice.repository.ProviderRepository;
import com.evcharging.providerservice.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ProviderRepository providerRepository;

    /** Gives every pre-existing provider account the single station its profile already described. */
    @EventListener(ApplicationReadyEvent.class)
    public void backfillStations() {
        for (Provider p : providerRepository.findAll()) {
            if (!stationRepository.existsByProviderId(p.getProviderId())) {
                stationRepository.save(fromProvider(p));
            }
        }
    }

    /** Creates the first station for a freshly registered account. */
    public Station createDefaultStation(Provider provider) {
        return stationRepository.save(fromProvider(provider));
    }

    public List<StationDTO> listForProvider(Long providerId, boolean activeOnly) {
        return stationRepository.findByProviderIdOrderByStationIdAsc(providerId)
                .stream()
                .filter(s -> !activeOnly || Boolean.TRUE.equals(s.getIsActive()))
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<StationDTO> listActive() {
        return stationRepository.findByIsActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public StationDTO get(Long stationId) {
        return toDTO(find(stationId));
    }

    public StationDTO create(Long providerId, StationDTO dto) {
        if (!providerRepository.existsById(providerId)) {
            throw new RuntimeException("Provider not found with id: " + providerId);
        }
        Station station = new Station();
        station.setProviderId(providerId);
        station.setIsActive(true);
        apply(station, dto);
        return toDTO(stationRepository.save(station));
    }

    public StationDTO update(Long stationId, StationDTO dto) {
        Station station = find(stationId);
        apply(station, dto);
        if (dto.getIsActive() != null) {
            station.setIsActive(dto.getIsActive());
        }
        return toDTO(stationRepository.save(station));
    }

    /** Soft delete, like accounts -- existing bookings keep pointing at a real row. */
    public void deactivate(Long stationId) {
        Station station = find(stationId);
        station.setIsActive(false);
        stationRepository.save(station);
    }

    public Long ownerOf(Long stationId) {
        return find(stationId).getProviderId();
    }

    private Station find(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + stationId));
    }

    private Station fromProvider(Provider p) {
        Station s = new Station();
        s.setProviderId(p.getProviderId());
        s.setName(p.getCompanyName());
        s.setAddress(p.getAddress());
        s.setCity(p.getCity());
        s.setState(p.getState());
        s.setZipCode(p.getZipCode());
        s.setLatitude(p.getLatitude());
        s.setLongitude(p.getLongitude());
        s.setRatePerHour(p.getRatePerHour());
        s.setChargerType(p.getChargerType());
        s.setMaxConcurrentCharges(p.getMaxConcurrentCharges() != null ? p.getMaxConcurrentCharges() : 1);
        s.setIsActive(Boolean.TRUE.equals(p.getIsActive()));
        return s;
    }

    private void apply(Station s, StationDTO d) {
        s.setName(d.getName());
        s.setAddress(d.getAddress());
        s.setCity(d.getCity());
        s.setState(d.getState());
        s.setZipCode(d.getZipCode());
        s.setLatitude(d.getLatitude());
        s.setLongitude(d.getLongitude());
        s.setRatePerHour(d.getRatePerHour());
        s.setChargerType(d.getChargerType());
        s.setMaxConcurrentCharges(d.getMaxConcurrentCharges() != null ? d.getMaxConcurrentCharges() : 1);
    }

    private StationDTO toDTO(Station s) {
        StationDTO dto = new StationDTO();
        dto.setStationId(s.getStationId());
        dto.setProviderId(s.getProviderId());
        dto.setCompanyName(providerRepository.findById(s.getProviderId()).map(Provider::getCompanyName).orElse(null));
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());
        dto.setCity(s.getCity());
        dto.setState(s.getState());
        dto.setZipCode(s.getZipCode());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setRatePerHour(s.getRatePerHour());
        dto.setChargerType(s.getChargerType());
        dto.setMaxConcurrentCharges(s.getMaxConcurrentCharges());
        dto.setIsActive(s.getIsActive());
        return dto;
    }
}
