package com.evcharging.providerservice.repository;

import com.evcharging.providerservice.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByProviderIdOrderByStationIdAsc(Long providerId);
    List<Station> findByIsActiveTrue();
    boolean existsByProviderId(Long providerId);
}
