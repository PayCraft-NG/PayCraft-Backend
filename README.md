# PayCraft-Backend

Product Requirement Document: Paycraft Payroll Application

Introduction

Paycraft is a payroll management application designed to streamline and automate the process of handling employee payments for businesses. The MVP will focus on providing employer and company management, employee profiles, payroll generation, virtual account services, and USSD support.


Features

1. Employer Management

The Employer Management feature allows users to manage their profiles and company information within the system.

Create Employer Profile: A user can create an employer profile, which includes basic details such as name, email, contact information, etc.

Update Employer Profile: Employers can update their profile information as needed.

Retrieve Employer Profile: Employers can fetch their profile information from the system.

Delete Employer Profile: Employers can delete their profile, which will also cascade to the associated company and employees.


Relationships:

Employer-Company Relationship:

For the MVP, an employer can have only one company, but this relationship will eventually scale to allow multiple companies for each employer.

2. Company Management

The Company Management feature allows an employer to create and manage a company.

Create Company Profile: The employer can create a company profile which includes details like the company's name, industry, location, etc.

Update Company Profile: The employer can update the company’s profile information as needed.

Retrieve Company Profile: Employers can fetch the company details.

Delete Company Profile: Employers can delete a company. Deleting a company will also remove all associated employees from the system.

Relationships:

Company-Employee Relationship:
Each company can have multiple employees under its profile.

3. Employee Management

Employee Management deals with managing the profiles of individuals working under a company.

Create Employee Profile: The employer can create profiles for employees, including details like name, role, salary, bank information, etc.

Update Employee Profile: Employers can update employee details, such as salary, role, or contact information.

Retrieve Employee Profile: Employers can fetch employee profile details.

Delete Employee Profile: Employers can delete employee profiles.


Relationship:

Employees are directly linked to a company profile.


4. Payroll Management

The Payroll feature allows companies to handle payment processing for their employees, either automatically or manually.

Create Payroll: Employers can create a payroll schedule that outlines how and when employees should be paid.

Add/Remove Employees from Payroll: Employers can add or remove employees from the payroll based on their eligibility or employment status.

Automatic Payroll: Payroll can be set to automatically run on a predefined schedule (e.g., monthly or bi-weekly).

Manual Payroll: Employers can manually trigger payroll runs by clicking the “Run Payroll” button.

Payroll Reporting: Employers can view payroll reports which show which employees have been paid and when.


5. Virtual Account Service

The Virtual Account Service allows employers to fund accounts within the application that will be used to process payrolls.

Fund Virtual Account: Employers can fund a virtual account on the application.

Automated Transfer: Upon running payroll (manually or automatically), funds are withdrawn from the employer's virtual account and transferred to the accounts of the employees.

Account Balance: Employers can check the balance of their virtual account at any time.


6. USSD Service

The USSD Service allows offline access to most of the application’s features. This feature is designed for users without internet access to manage payroll operations via USSD codes.

Employer and Company Management: Employers can manage their profiles, companies, and employees via USSD.

Payroll Management: Employers can create, run, and view payroll details via USSD.

Account Balance: Employers can check virtual account balances via USSD.

Employee Management: Employers can manage employee profiles and payrolls through USSD.

Note: this feature isn’t live

Non-Functional Requirements

Security: Data encryption for sensitive information like bank details. Authentication and role-based authorization to ensure secure access to features.

Performance: The system must handle large volumes of employees and payrolls efficiently, with an average response time of under 2 seconds for profile and payroll management operations.

Scalability: The application must scale to allow future support for multiple companies under one employer.

Reliability: Payrolls should always run as scheduled ensuring reliability of payments.


