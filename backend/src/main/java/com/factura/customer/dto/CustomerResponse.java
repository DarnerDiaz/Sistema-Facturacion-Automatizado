package com.factura.customer.dto;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String taxId,
        String email,
        String address,
        String phone,
        boolean active
) {
}
