package com.evcharging.providerservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MfaCodeRequest {
    @NotBlank(message = "Verification code is required")
    private String code;
}
