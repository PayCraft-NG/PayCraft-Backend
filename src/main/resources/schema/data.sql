CREATE TABLE Employer (
    employerId UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(100) NOT NULL,
    phone_number VARCHAR(100) NOT NULL,
    street_address VARCHAR(100) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    bvn VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE Company (
    companyId UUID PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    company_size ENUM('SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE') NOT NULL,
    company_email_address VARCHAR(100) NOT NULL,
    company_phone_number VARCHAR(100) NOT NULL,
    company_street_address VARCHAR(100) NOT NULL,
    company_country VARCHAR(100) NOT NULL,
    company_currency ENUM('NGN', 'USD') NOT NULL,
    employerId UUID NOT NULL,
    FOREIGN KEY (employerId) REFERENCES Employer(employerId)
);

CREATE TABLE Employee (
    employeeId UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    email_address VARCHAR(100) NOT NULL,
    phone_number VARCHAR(100) NOT NULL,
    street_address VARCHAR(100) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    bvn VARCHAR(100) NOT NULL,
    bankName VARCHAR(100) NOT NULL,
    accountNumber VARCHAR(100) NOT NULL,
    salaryAmount DECIMAL(15, 2) NOT NULL,
    salaryCurrency ENUM('NGN', 'USD') NOT NULL,
    companyId UUID NOT NULL,
    FOREIGN KEY (companyId) REFERENCES Company(companyId)
);

CREATE TABLE Notification (
    id UUID PRIMARY KEY,
    notification_date TIMESTAMP NOT NULL,
    message TEXT NOT NULL,
    visible BOOLEAN DEFAULT TRUE,
    companyId UUID NOT NULL,
    FOREIGN KEY (companyId) REFERENCES Company(companyId)
);

CREATE TABLE Payroll (
    payrollId UUID PRIMARY KEY,
    payPeriodStart DATE NOT NULL,
    payPeriodEnd DATE NOT NULL,
    grossSalary DECIMAL(10, 2) NOT NULL,
    netSalary DECIMAL(10, 2) NOT NULL,
    paymentDate DATE NOT NULL,
    payStatus ENUM('PAID', 'PENDING', 'FAILED') NOT NULL,
    companyId UUID NOT NULL,
    FOREIGN KEY (companyId) REFERENCES Company(companyId)
);

CREATE TABLE Payment (
    paymentId UUID PRIMARY KEY,
    paymentMethod ENUM('USSD', 'WEB') NOT NULL,
    paymentDate DATE NOT NULL,
    transactionId VARCHAR(100) NOT NULL,
    paymentStatus ENUM('PAID', 'PENDING', 'FAILED') NOT NULL,
    payrollId UUID NOT NULL,
    FOREIGN KEY (payrollId) REFERENCES Payroll(payrollId)
);
