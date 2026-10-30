package com.evcharging.providerservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MfaLoginRequest {
    @NotBlank(message = "Sign-in token is required")
    private String mfaToken;

    @NotBlank(message = "Verification code is required")
    private String code;
}
