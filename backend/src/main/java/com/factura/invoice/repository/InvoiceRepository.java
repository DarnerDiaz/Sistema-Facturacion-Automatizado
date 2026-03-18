package com.factura.invoice.repository;

import com.factura.invoice.Invoice;
import com.factura.invoice.InvoiceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    List<Invoice> findByCompanyIdAndStatusOrderByCreatedAtDesc(UUID companyId, InvoiceStatus status);

    Optional<Invoice> findByIdAndCompanyId(UUID id, UUID companyId);

    long countByCompanyId(UUID companyId);
}
