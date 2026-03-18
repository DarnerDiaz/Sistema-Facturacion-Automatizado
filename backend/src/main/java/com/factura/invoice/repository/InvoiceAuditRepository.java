package com.factura.invoice.repository;

import com.factura.invoice.InvoiceAudit;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceAuditRepository extends JpaRepository<InvoiceAudit, UUID> {

    List<InvoiceAudit> findByInvoiceIdOrderByChangedAtAsc(UUID invoiceId);
}
