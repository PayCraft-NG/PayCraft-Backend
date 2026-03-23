-- Initial PayCraft schema (MySQL 8+).
-- Matches JPA entities with Hibernate 6 default physical naming (camelCase -> snake_case).
-- Table names from @Table / entity names: employer, company, employee, payroll, virtual_accounts,
-- payments, cards, notification, auth_token, webhook_data.

SET NAMES utf8mb4;

CREATE TABLE employer (
    employer_id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    updated_by VARCHAR(255) NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    bvn VARCHAR(255) NOT NULL,
    password VARCHAR(255) NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (employer_id),
    CONSTRAINT uk_employer_email UNIQUE (email_address),
    CONSTRAINT uk_employer_phone UNIQUE (phone_number),
    CONSTRAINT uk_employer_bvn UNIQUE (bvn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE company (
    company_id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    updated_by VARCHAR(255) NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE employee (
    employee_id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    updated_by VARCHAR(255) NULL,
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
    salary_amount DECIMAL(15, 2) NOT NULL,
    salary_currency VARCHAR(10) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (employee_id),
    CONSTRAINT uk_employee_email UNIQUE (email_address),
    CONSTRAINT uk_employee_phone UNIQUE (phone_number),
    CONSTRAINT uk_employee_bvn UNIQUE (bvn),
    CONSTRAINT fk_employee_company FOREIGN KEY (company_id) REFERENCES company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE virtual_accounts (
    account_id VARCHAR(36) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payroll (
    payroll_id VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    updated_by VARCHAR(255) NULL,
    payroll_name VARCHAR(255) NOT NULL,
    automatic TINYINT(1) NOT NULL DEFAULT 0,
    total_salary DECIMAL(19, 2) NULL,
    last_run_date DATE NULL,
    pay_period_start DATE NULL,
    pay_period_end DATE NULL,
    cron_expression VARCHAR(255) NULL,
    payment_status VARCHAR(255) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payroll_id),
    CONSTRAINT uk_payroll_name UNIQUE (payroll_name),
    CONSTRAINT fk_payroll_company FOREIGN KEY (company_id) REFERENCES company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payroll_employee (
    payroll_id VARCHAR(36) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payroll_id, employee_id),
    CONSTRAINT fk_pe_payroll FOREIGN KEY (payroll_id) REFERENCES payroll (payroll_id),
    CONSTRAINT fk_pe_employee FOREIGN KEY (employee_id) REFERENCES employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    payment_id VARCHAR(36) NOT NULL,
    reference_number VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    transaction_date_time DATETIME(6) NOT NULL,
    description VARCHAR(255) NULL,
    currency VARCHAR(255) NOT NULL,
    payroll_name VARCHAR(255) NULL,
    employee_name VARCHAR(255) NULL,
    account_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES virtual_accounts (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cards (
    card_id BIGINT NOT NULL AUTO_INCREMENT,
    card_number VARCHAR(255) NOT NULL,
    expiry_month VARCHAR(255) NOT NULL,
    expiry_year VARCHAR(255) NOT NULL,
    cvv VARCHAR(255) NOT NULL,
    card_pin VARCHAR(255) NOT NULL,
    account_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (card_id),
    CONSTRAINT uk_cards_card_number UNIQUE (card_number),
    CONSTRAINT fk_cards_account FOREIGN KEY (account_id) REFERENCES virtual_accounts (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification (
    id VARCHAR(36) NOT NULL,
    notification_date DATETIME(6) NOT NULL,
    message VARCHAR(255) NOT NULL,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    company_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_company FOREIGN KEY (company_id) REFERENCES company (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    access_token VARCHAR(2048) NOT NULL,
    refresh_token VARCHAR(2048) NOT NULL,
    expired TINYINT(1) NOT NULL DEFAULT 0,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NULL,
    employer_id VARCHAR(36) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_access UNIQUE (access_token),
    CONSTRAINT uk_auth_refresh UNIQUE (refresh_token),
    CONSTRAINT fk_auth_employer FOREIGN KEY (employer_id) REFERENCES employer (employer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE webhook_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event VARCHAR(255) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    fee DECIMAL(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
