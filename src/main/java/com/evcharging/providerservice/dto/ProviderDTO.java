package com.evcharging.providerservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    /** Only used on registration; validated in ProviderService, not here, since this DTO is reused for updates. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
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
    /** Read-only: whether this account requires a second factor at sign-in. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean mfaEnabled;
    /** Write-only: a new PAN. Blank keeps the one on file. Returned only masked, as panMasked. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String panNumber;
    /** Write-only: a new Aadhaar number. Blank keeps the one on file. Returned only masked. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String aadhaarNumber;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String panMasked;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String aadhaarMasked;
    /** Optional profile photo (image data URL). Empty string removes it; absent keeps it. */
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
