package com.factura.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceCreateRequest(
        @NotNull UUID customerId,
        @NotNull LocalDate issueDate,
        LocalDate dueDate,
        String notes,
        @NotEmpty List<@Valid InvoiceItemRequest> items
) {
}
