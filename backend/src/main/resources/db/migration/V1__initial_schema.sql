CREATE TABLE companies (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    address TEXT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    company_id BINARY(16) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE customers (
    id BINARY(16) PRIMARY KEY,
    company_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) NOT NULL,
    email VARCHAR(255) NULL,
    address TEXT NULL,
    phone VARCHAR(20) NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_customers_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT uk_customer_company_tax UNIQUE (company_id, tax_id)
);

CREATE TABLE invoices (
    id BINARY(16) PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL,
    company_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) NOT NULL,
    total DECIMAL(15,2) NOT NULL,
    notes TEXT NULL,
    pdf_url VARCHAR(500) NULL,
    created_by BINARY(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoices_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_invoices_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uk_invoice_company_number UNIQUE (company_id, invoice_number)
);

CREATE TABLE invoice_items (
    id BINARY(16) PRIMARY KEY,
    invoice_id BINARY(16) NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    tax_percentage DECIMAL(5,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    sequence INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT uk_invoice_item_sequence UNIQUE (invoice_id, sequence)
);

CREATE TABLE invoice_audit (
    id BINARY(16) PRIMARY KEY,
    invoice_id BINARY(16) NOT NULL,
    old_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by BINARY(16) NOT NULL,
    reason TEXT NULL,
    ip_address VARCHAR(45) NULL,
    changed_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_audit_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_invoice_audit_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE TABLE email_logs (
    id BINARY(16) PRIMARY KEY,
    invoice_id BINARY(16) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT NULL,
    attempts INT NOT NULL,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_email_logs_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_invoices_status_created_at ON invoices(status, created_at);
CREATE INDEX idx_invoice_audit_invoice_changed_at ON invoice_audit(invoice_id, changed_at);
CREATE INDEX idx_email_logs_status_created_at ON email_logs(status, created_at);
