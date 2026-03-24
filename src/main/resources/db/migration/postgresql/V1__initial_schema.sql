-- Initial PayCraft schema (PostgreSQL 14+).
-- Same logical model as MySQL V1; Hibernate validate uses PostgreSQLDialect.

CREATE TABLE employer (
    employer_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    bvn VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6),
    PRIMARY KEY (employer_id),
    CONSTRAINT uk_employer_email UNIQUE (email_address),
    CONSTRAINT uk_employer_phone UNIQUE (phone_number),
    CONSTRAINT uk_employer_bvn UNIQUE (bvn)
);

CREATE TABLE company (
    company_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    company_name VARCHAR(100) NOT NULL,
    company_size VARCHAR(10) NOT NULL,
    company_email_address VARCHAR(100) NOT NULL,
    company_phone_number VARCHAR(100) NOT NULL,
    company_street_address VARCHAR(100) NOT NULL,
    company_country VARCHAR(100) NOT NULL,
    company_currency VARCHAR(10) NOT NULL,
    employer_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (company_id),
    CONSTRAINT fk_company_employer FOREIGN KEY (employer_id) REFERENCES employer (employer_id)
);

CREATE TABLE employee (
    employee_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    email_address VARCHAR(100) NOT NULL,
    phone_number VARCHAR(100) NOT NULL,
    street_address VARCHAR(100) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    bvn VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    salary_amount NUMERIC(15, 2) NOT NULL,
    salary_currency VARCHAR(10) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6),
    PRIMARY KEY (employee_id),
    CONSTRAINT uk_employee_email UNIQUE (email_address),
    CONSTRAINT uk_employee_phone UNIQUE (phone_number),
    CONSTRAINT uk_employee_bvn UNIQUE (bvn),
    CONSTRAINT fk_employee_company FOREIGN KEY (company_id) REFERENCES company (company_id)
);

CREATE TABLE virtual_accounts (
    account_id VARCHAR(36) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    account_reference VARCHAR(255) NOT NULL,
    kora_account_reference VARCHAR(255) NOT NULL,
    bank_code VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_status VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    employer_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (account_id),
    CONSTRAINT uk_va_account_number UNIQUE (account_number),
    CONSTRAINT uk_va_account_reference UNIQUE (account_reference),
    CONSTRAINT uk_va_kora_reference UNIQUE (kora_account_reference),
    CONSTRAINT uk_va_employer UNIQUE (employer_id),
    CONSTRAINT fk_va_employer FOREIGN KEY (employer_id) REFERENCES employer (employer_id)
);

CREATE TABLE payroll (
    payroll_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    payroll_name VARCHAR(255) NOT NULL,
    automatic BOOLEAN NOT NULL DEFAULT FALSE,
    total_salary NUMERIC(19, 2),
    last_run_date DATE,
    pay_period_start DATE,
    pay_period_end DATE,
    cron_expression VARCHAR(255),
    payment_status VARCHAR(255) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payroll_id),
    CONSTRAINT uk_payroll_name UNIQUE (payroll_name),
    CONSTRAINT fk_payroll_company FOREIGN KEY (company_id) REFERENCES company (company_id)
);

CREATE TABLE payroll_employee (
    payroll_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payroll_id, employee_id),
    CONSTRAINT fk_pe_payroll FOREIGN KEY (payroll_id) REFERENCES payroll (payroll_id),
    CONSTRAINT fk_pe_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
);

CREATE TABLE payments (
    payment_id VARCHAR(36) NOT NULL,
    reference_number VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    transaction_date_time TIMESTAMP(6) NOT NULL,
    description VARCHAR(255),
    currency VARCHAR(255) NOT NULL,
    payroll_name VARCHAR(255),
    employee_name VARCHAR(255),
    account_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES virtual_accounts (account_id)
);

CREATE TABLE cards (
    card_id BIGSERIAL PRIMARY KEY,
    card_number VARCHAR(255) NOT NULL,
    expiry_month VARCHAR(255) NOT NULL,
    expiry_year VARCHAR(255) NOT NULL,
    cvv VARCHAR(255) NOT NULL,
    card_pin VARCHAR(255) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    CONSTRAINT uk_cards_card_number UNIQUE (card_number),
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES virtual_accounts (account_id)
);

CREATE TABLE notification (
    id VARCHAR(36) NOT NULL,
    notification_date TIMESTAMP(6) NOT NULL,
    message VARCHAR(255) NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    company_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_company FOREIGN KEY (company_id) REFERENCES company (company_id)
);

CREATE TABLE auth_token (
    id BIGSERIAL PRIMARY KEY,
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512) NOT NULL,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6),
    employer_id VARCHAR(36),
    CONSTRAINT uk_auth_access UNIQUE (access_token),
    CONSTRAINT uk_auth_refresh UNIQUE (refresh_token),
    CONSTRAINT fk_auth_employer FOREIGN KEY (employer_id) REFERENCES employer (employer_id)
);

CREATE TABLE webhook_data (
    id BIGSERIAL PRIMARY KEY,
    event VARCHAR(255) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    fee NUMERIC(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL
);
