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
