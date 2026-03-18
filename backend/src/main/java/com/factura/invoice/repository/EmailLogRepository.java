package com.factura.invoice.repository;

import com.factura.invoice.EmailLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

    List<EmailLog> findByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);
}
