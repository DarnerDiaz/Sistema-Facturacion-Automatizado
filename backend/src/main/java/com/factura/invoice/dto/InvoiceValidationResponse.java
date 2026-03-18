package com.factura.invoice.dto;

import java.util.List;

public record InvoiceValidationResponse(
        boolean valid,
        List<String> errors
) {
}
