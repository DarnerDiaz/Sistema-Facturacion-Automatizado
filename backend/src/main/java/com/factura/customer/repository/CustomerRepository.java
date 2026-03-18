package com.factura.customer.repository;

import com.factura.customer.Customer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByCompanyIdAndActiveTrue(UUID companyId);
}
