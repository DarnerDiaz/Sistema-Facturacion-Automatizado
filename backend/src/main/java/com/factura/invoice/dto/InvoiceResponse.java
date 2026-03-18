package com.factura.invoice.dto;

import com.factura.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID customerId,
        String customerName,
        InvoiceStatus status,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal total,
        String notes,
        String pdfUrl,
        List<InvoiceResponseItem> items
) {
    public record InvoiceResponseItem(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal taxPercentage,
            BigDecimal lineTotal,
            Integer sequence
    ) {
    }
}
