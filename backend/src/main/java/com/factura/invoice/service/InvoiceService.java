package com.factura.invoice.service;

import com.factura.auth.support.AuthenticatedUserService;
import com.factura.common.exception.ApiException;
import com.factura.customer.Customer;
import com.factura.customer.repository.CustomerRepository;
import com.factura.invoice.Invoice;
import com.factura.invoice.InvoiceAudit;
import com.factura.invoice.InvoiceItem;
import com.factura.invoice.InvoiceStatus;
import com.factura.invoice.dto.InvoiceCreateRequest;
import com.factura.invoice.dto.InvoiceResponse;
import com.factura.invoice.dto.InvoiceValidationResponse;
import com.factura.invoice.repository.InvoiceAuditRepository;
import com.factura.invoice.repository.InvoiceRepository;
import com.factura.user.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceAuditRepository invoiceAuditRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            InvoiceAuditRepository invoiceAuditRepository,
            CustomerRepository customerRepository,
            AuthenticatedUserService authenticatedUserService,
            InvoicePdfService invoicePdfService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceAuditRepository = invoiceAuditRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.invoicePdfService = invoicePdfService;
    }

    @Transactional
    public InvoiceResponse createDraft(InvoiceCreateRequest request) {
        User user = authenticatedUserService.requireCurrentUser();
        Customer customer = customerRepository.findById(request.customerId())
                .filter(value -> value.getCompany().getId().equals(user.getCompany().getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Customer not found for current company"));

        List<String> validationErrors = validatePayload(request, customer);
        if (!validationErrors.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.join("; ", validationErrors));
        }

        Invoice invoice = new Invoice();
        invoice.setCompany(user.getCompany());
        invoice.setCustomer(customer);
        invoice.setCreatedBy(user);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setNotes(request.notes());
        invoice.setInvoiceNumber(buildNextInvoiceNumber(user.getCompany().getId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();
        for (int i = 0; i < request.items().size(); i++) {
            var itemRequest = request.items().get(i);
            BigDecimal lineBase = itemRequest.quantity().multiply(itemRequest.unitPrice());
            BigDecimal lineTax = lineBase.multiply(itemRequest.taxPercentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineBase.add(lineTax).setScale(2, RoundingMode.HALF_UP);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setDescription(itemRequest.description());
            item.setQuantity(itemRequest.quantity().setScale(2, RoundingMode.HALF_UP));
            item.setUnitPrice(itemRequest.unitPrice().setScale(2, RoundingMode.HALF_UP));
            item.setTaxPercentage(itemRequest.taxPercentage().setScale(2, RoundingMode.HALF_UP));
            item.setLineTotal(lineTotal);
            item.setSequence(i + 1);
            items.add(item);

            subtotal = subtotal.add(lineBase);
            taxAmount = taxAmount.add(lineTax);
        }

        invoice.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        invoice.setTaxAmount(taxAmount.setScale(2, RoundingMode.HALF_UP));
        invoice.setTotal(invoice.getSubtotal().add(invoice.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
        invoice.getItems().clear();
        invoice.getItems().addAll(items);

        Invoice saved = invoiceRepository.save(invoice);
        saveAudit(saved, null, InvoiceStatus.DRAFT.name(), user, "Factura creada como borrador", null);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> list(InvoiceStatus status) {
        UUID companyId = authenticatedUserService.currentCompanyId();
        List<Invoice> invoices = status == null
                ? invoiceRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                : invoiceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);

        return invoices.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        UUID companyId = authenticatedUserService.currentCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));
        return toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse emit(UUID invoiceId, String reason, String ipAddress) {
        User user = authenticatedUserService.requireCurrentUser();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, user.getCompany().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "Only DRAFT invoices can be emitted");
        }

        String pdfPath = invoicePdfService.generateAndStorePdf(invoice);
        invoice.setPdfUrl(pdfPath);
        invoice.setStatus(InvoiceStatus.ISSUED);
        Invoice saved = invoiceRepository.save(invoice);
        saveAudit(saved, InvoiceStatus.DRAFT.name(), InvoiceStatus.ISSUED.name(), user,
                reason == null || reason.isBlank() ? "Emision manual" : reason,
                ipAddress);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public byte[] getPdf(UUID invoiceId) {
        UUID companyId = authenticatedUserService.currentCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));

        if (invoice.getPdfUrl() == null || invoice.getPdfUrl().isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Invoice PDF not available yet");
        }

        return invoicePdfService.loadPdf(invoice.getPdfUrl());
    }

    @Transactional(readOnly = true)
    public InvoiceValidationResponse validate(InvoiceCreateRequest request) {
        User user = authenticatedUserService.requireCurrentUser();
        Customer customer = customerRepository.findById(request.customerId())
                .filter(value -> value.getCompany().getId().equals(user.getCompany().getId()))
                .orElse(null);

        List<String> errors = validatePayload(request, customer);
        return new InvoiceValidationResponse(errors.isEmpty(), errors);
    }

    private List<String> validatePayload(InvoiceCreateRequest request, Customer customer) {
        List<String> errors = new ArrayList<>();

        if (customer == null) {
            errors.add("Cliente no encontrado en la empresa actual");
        } else if (!customer.isActive()) {
            errors.add("Cliente inactivo");
        }

        if (request.items() == null || request.items().isEmpty()) {
            errors.add("La factura debe tener al menos un item");
            return errors;
        }

        for (int i = 0; i < request.items().size(); i++) {
            var item = request.items().get(i);
            if (item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Item " + (i + 1) + ": cantidad debe ser mayor a 0");
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Item " + (i + 1) + ": precio unitario debe ser mayor a 0");
            }
            if (item.taxPercentage().compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Item " + (i + 1) + ": impuesto no puede ser negativo");
            }
        }

        return errors;
    }

    private String buildNextInvoiceNumber(UUID companyId) {
        long nextSequence = invoiceRepository.countByCompanyId(companyId) + 1;
        return "F" + Year.now().getValue() + "-" + String.format("%08d", nextSequence);
    }

    private void saveAudit(Invoice invoice, String oldStatus, String newStatus, User user, String reason, String ipAddress) {
        InvoiceAudit audit = new InvoiceAudit();
        audit.setInvoice(invoice);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setChangedBy(user);
        audit.setReason(reason);
        audit.setIpAddress(ipAddress);
        invoiceAuditRepository.save(audit);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceResponse.InvoiceResponseItem> items = invoice.getItems()
                .stream()
                .map(item -> new InvoiceResponse.InvoiceResponseItem(
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTaxPercentage(),
                        item.getLineTotal(),
                        item.getSequence()
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getName(),
                invoice.getStatus(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotal(),
                invoice.getNotes(),
                invoice.getPdfUrl(),
                items
        );
    }
}
