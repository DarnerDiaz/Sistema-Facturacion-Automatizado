package com.factura.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InvoiceItemRequest(
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @NotNull @DecimalMin(value = "0.00") BigDecimal taxPercentage
) {
}
