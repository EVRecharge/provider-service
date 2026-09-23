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

    /** Null for legacy accounts registered before password auth. BCrypt hash, never the raw password. */
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
