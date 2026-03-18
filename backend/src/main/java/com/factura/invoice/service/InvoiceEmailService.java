package com.factura.invoice.service;

import com.factura.auth.support.AuthenticatedUserService;
import com.factura.common.exception.ApiException;
import com.factura.invoice.EmailLog;
import com.factura.invoice.EmailLogStatus;
import com.factura.invoice.Invoice;
import com.factura.invoice.InvoiceStatus;
import com.factura.invoice.repository.EmailLogRepository;
import com.factura.invoice.repository.InvoiceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceEmailService {

    private final InvoiceRepository invoiceRepository;
    private final EmailLogRepository emailLogRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final InvoicePdfService invoicePdfService;
    private final JavaMailSender javaMailSender;

    public InvoiceEmailService(
            InvoiceRepository invoiceRepository,
            EmailLogRepository emailLogRepository,
            AuthenticatedUserService authenticatedUserService,
            InvoicePdfService invoicePdfService,
            JavaMailSender javaMailSender
    ) {
        this.invoiceRepository = invoiceRepository;
        this.emailLogRepository = emailLogRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.invoicePdfService = invoicePdfService;
        this.javaMailSender = javaMailSender;
    }

    @Value("${app.mail.from:no-reply@facturacion.local}")
    private String mailFrom;

    @Transactional
    public Map<String, Object> sendInvoiceByEmail(UUID invoiceId) {
        UUID companyId = authenticatedUserService.currentCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invoice not found"));

        if (invoice.getCustomer().getEmail() == null || invoice.getCustomer().getEmail().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Customer does not have a valid email");
        }

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new ApiException(HttpStatus.CONFLICT, "Emit the invoice before sending by email");
        }

        EmailLog log = new EmailLog();
        log.setInvoice(invoice);
        log.setRecipientEmail(invoice.getCustomer().getEmail());
        log.setAttempts(1);
        log.setStatus(EmailLogStatus.PENDING);
        emailLogRepository.save(log);

        try {
            byte[] pdfBytes = invoice.getPdfUrl() == null
                    ? invoicePdfService.generatePdfBytes(invoice)
                    : invoicePdfService.loadPdf(invoice.getPdfUrl());

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailFrom);
            helper.setTo(invoice.getCustomer().getEmail());
            helper.setSubject("Factura " + invoice.getInvoiceNumber());
            helper.setText(buildHtmlBody(invoice), true);
            helper.addAttachment(invoice.getInvoiceNumber() + ".pdf", new ByteArrayResource(pdfBytes));

            javaMailSender.send(message);

            log.setStatus(EmailLogStatus.SENT);
            log.setSentAt(OffsetDateTime.now());
            invoice.setStatus(InvoiceStatus.SENT);
            invoiceRepository.save(invoice);
        } catch (MessagingException | RuntimeException ex) {
            log.setStatus(EmailLogStatus.FAILED);
            log.setErrorMessage(ex.getMessage());
            emailLogRepository.save(log);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to send email");
        }

        emailLogRepository.save(log);
        return Map.of(
                "status", "SENT",
                "recipient", invoice.getCustomer().getEmail(),
                "invoiceId", invoice.getId().toString()
        );
    }

    private String buildHtmlBody(Invoice invoice) {
        return "<h2>Factura Electronica</h2>"
                + "<p>Hola " + invoice.getCustomer().getName() + ",</p>"
                + "<p>Adjuntamos su factura <strong>" + invoice.getInvoiceNumber() + "</strong>.</p>"
                + "<p>Total: PEN " + invoice.getTotal().toPlainString() + "</p>"
                + "<p>Gracias por su preferencia.</p>";
    }
}
