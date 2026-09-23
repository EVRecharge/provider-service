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
