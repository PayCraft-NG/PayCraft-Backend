CREATE TABLE Employer (
    employerId VARCHAR(100) PRIMARY KEY AUTO_INCREMENT,
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
    companyId VARCHAR(100) PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL,
    company_size ENUM('small', 'medium', 'large', 'enterprise') NOT NULL,
    company_email_address VARCHAR(100) NOT NULL,
    company_phone_number VARCHAR(100) NOT NULL,
    company_street_address VARCHAR(100) NOT NULL,
    company_country VARCHAR(100) NOT NULL,
    company_currency ENUM('naira', 'dollar') NOT NULL,
    employerId VARCHAR(100) NOT NULL,
    FOREIGN KEY (employerId) REFERENCES Employer(employerId)
);

CREATE TABLE Employee (
    employeeId VARCHAR(100) PRIMARY KEY AUTO_INCREMENT,
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
    salaryCurrency ENUM('naira', 'dollar') NOT NULL,
    companyId VARCHAR(100) NOT NULL,
    FOREIGN KEY (companyId) REFERENCES Company(companyId)
);
