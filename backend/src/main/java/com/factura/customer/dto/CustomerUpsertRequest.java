package com.factura.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerUpsertRequest(
        @NotBlank String name,
        @NotBlank String taxId,
        @Email String email,
        String address,
        String phone
) {
}
