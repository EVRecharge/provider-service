package com.evcharging.providerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {
    private Long stationId;
    private Long providerId;
    /** Owning account's company name -- read-only, filled in for driver-facing search results. */
    private String companyName;
    @NotBlank(message = "Station name is required")
    private String name;
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
    @Min(value = 1, message = "At least one charging slot is required")
    private Integer maxConcurrentCharges;
    private Boolean isActive;
}
