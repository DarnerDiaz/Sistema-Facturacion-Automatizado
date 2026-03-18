package com.factura.invoice.controller;

import com.factura.invoice.InvoiceStatus;
import com.factura.invoice.dto.InvoiceCreateRequest;
import com.factura.invoice.dto.InvoiceResponse;
import com.factura.invoice.dto.InvoiceValidationResponse;
import com.factura.invoice.service.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse create(@Valid @RequestBody InvoiceCreateRequest request) {
        return invoiceService.createDraft(request);
    }

    @GetMapping
    public List<InvoiceResponse> list(@RequestParam(required = false) InvoiceStatus status) {
        return invoiceService.list(status);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getById(@PathVariable UUID id) {
        return invoiceService.getById(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable UUID id) {
        byte[] bytes = invoiceService.getPdf(id);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(resource);
    }

    @PostMapping("/validate")
    public InvoiceValidationResponse validatePayload(@Valid @RequestBody InvoiceCreateRequest request) {
        return invoiceService.validate(request);
    }

    @PostMapping("/{id}/emit")
    public InvoiceResponse emit(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> payload,
            HttpServletRequest request
    ) {
        String reason = payload == null ? null : payload.get("reason");
        return invoiceService.emit(id, reason, request.getRemoteAddr());
    }
}
