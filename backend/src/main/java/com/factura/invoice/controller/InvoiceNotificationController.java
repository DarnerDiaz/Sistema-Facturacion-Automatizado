package com.factura.invoice.controller;

import com.factura.invoice.service.InvoiceEmailService;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceNotificationController {

    private final InvoiceEmailService invoiceEmailService;

    public InvoiceNotificationController(InvoiceEmailService invoiceEmailService) {
        this.invoiceEmailService = invoiceEmailService;
    }

    @PostMapping("/{id}/send-email")
    public Map<String, Object> sendEmail(@PathVariable UUID id) {
        return invoiceEmailService.sendInvoiceByEmail(id);
    }
}
