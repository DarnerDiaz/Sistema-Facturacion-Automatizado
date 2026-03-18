package com.factura.customer.service;

import com.factura.auth.support.AuthenticatedUserService;
import com.factura.common.exception.ApiException;
import com.factura.customer.Customer;
import com.factura.customer.dto.CustomerResponse;
import com.factura.customer.dto.CustomerUpsertRequest;
import com.factura.customer.repository.CustomerRepository;
import com.factura.user.User;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerService(
            CustomerRepository customerRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public CustomerResponse create(CustomerUpsertRequest request) {
        User user = authenticatedUserService.requireCurrentUser();

        Customer customer = new Customer();
        customer.setCompany(user.getCompany());
        customer.setName(request.name());
        customer.setTaxId(request.taxId());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setPhone(request.phone());
        customer.setActive(true);

        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listActive() {
        UUID companyId = authenticatedUserService.currentCompanyId();
        return customerRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        UUID companyId = authenticatedUserService.currentCompanyId();
        Customer customer = customerRepository.findById(id)
                .filter(value -> value.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getTaxId(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getPhone(),
                customer.isActive()
        );
    }
}
