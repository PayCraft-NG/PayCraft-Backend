# PayCraft Backend - Postman API Collection

## 📋 Overview
This document provides detailed API endpoint documentation for the PayCraft Backend system. You can import these endpoints into Postman for testing.

## 🔑 Authentication Setup

### Environment Variables
Create these variables in Postman:
- `BASE_URL`: `http://localhost:6020` (or your deployed URL)
- `JWT_TOKEN`: Will be set after login
- `EMPLOYER_ID`: Will be set after creating employer
- `COMPANY_ID`: Will be set after creating company
- `EMPLOYEE_ID`: Will be set after creating employee
- `PAYROLL_ID`: Will be set after creating payroll

## 📋 Complete API Endpoints

### 1. Authentication APIs

#### 1.1 Login
```http
POST {{BASE_URL}}/api/v1/auth/login
Content-Type: application/json

{
    "emailAddress": "employer@example.com",
    "password": "password123"
}
```

**Response:**
```json
{
    "statusCode": "200",
    "statusMessage": "Login Successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1...",
        "refreshToken": "eyJhbGciOiJIUzI1...",
        "tokenType": "Bearer",
        "expiresIn": 3600
    }
}
```

#### 1.2 Refresh Token
```http
POST {{BASE_URL}}/api/v1/auth/refresh-token
Content-Type: application/json

{
    "refreshToken": "{{REFRESH_TOKEN}}"
}
```

### 2. Employer Management APIs

#### 2.1 Create Employer
```http
POST {{BASE_URL}}/api/v1/employer/create
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "emailAddress": "john.doe@example.com",
    "phoneNumber": "+234901234567",
    "streetAddress": "123 Main Street, Lagos",
    "jobTitle": "CEO",
    "bvn": "22234567890",
    "password": "securePassword123"
}
```

#### 2.2 Get Employer Details
```http
GET {{BASE_URL}}/api/v1/employer/details
Authorization: Bearer {{JWT_TOKEN}}
```

#### 2.3 Update Employer
```http
PUT {{BASE_URL}}/api/v1/employer/update
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe Updated",
    "phoneNumber": "+234901234568",
    "streetAddress": "456 New Street, Lagos",
    "jobTitle": "Chief Executive Officer"
}
```

#### 2.4 Update Employer Password
```http
PATCH {{BASE_URL}}/api/v1/employer/update/password
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "currentPassword": "oldPassword123",
    "newPassword": "newSecurePassword123",
    "confirmPassword": "newSecurePassword123"
}
```

#### 2.5 Delete Employer
```http
DELETE {{BASE_URL}}/api/v1/employer/delete
Authorization: Bearer {{JWT_TOKEN}}
```

### 3. Company Management APIs

#### 3.1 Create Company
```http
POST {{BASE_URL}}/api/v1/company/create?employerId={{EMPLOYER_ID}}
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "companyName": "Tech Solutions Ltd",
    "companySize": "MEDIUM",
    "companyEmailAddress": "info@techsolutions.com",
    "companyPhoneNumber": "+234901234567",
    "companyStreetAddress": "123 Business District, Lagos",
    "companyCountry": "Nigeria",
    "companyCurrency": "NGN"
}
```

#### 3.2 Get Company Details
```http
GET {{BASE_URL}}/api/v1/company/details
Authorization: Bearer {{JWT_TOKEN}}
```

#### 3.3 Update Company
```http
PUT {{BASE_URL}}/api/v1/company/update
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "companyName": "Tech Solutions Limited",
    "companySize": "LARGE",
    "companyEmailAddress": "contact@techsolutions.com",
    "companyPhoneNumber": "+234901234568"
}
```

#### 3.4 Delete Company
```http
DELETE {{BASE_URL}}/api/v1/company/delete
Authorization: Bearer {{JWT_TOKEN}}
```

### 4. Employee Management APIs

#### 4.1 Create Employee
```http
POST {{BASE_URL}}/api/v1/employee/create
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "firstName": "Jane",
    "lastName": "Smith",
    "dateOfBirth": "1990-05-15",
    "emailAddress": "jane.smith@techsolutions.com",
    "phoneNumber": "+234901234569",
    "streetAddress": "789 Employee Street, Lagos",
    "jobTitle": "Software Developer",
    "department": "Engineering",
    "bvn": "12345678901",
    "bankName": "First Bank",
    "accountNumber": "1234567890",
    "salaryAmount": 250000.00,
    "salaryCurrency": "NGN"
}
```

#### 4.2 Get All Employees
```http
GET {{BASE_URL}}/api/v1/employee/all
Authorization: Bearer {{JWT_TOKEN}}
```

#### 4.3 Get Employee by ID
```http
GET {{BASE_URL}}/api/v1/employee/{{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 4.4 Update Employee
```http
PUT {{BASE_URL}}/api/v1/employee/{{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "firstName": "Jane",
    "lastName": "Smith-Johnson",
    "emailAddress": "jane.smith@techsolutions.com",
    "phoneNumber": "+234901234570",
    "jobTitle": "Senior Software Developer",
    "salaryAmount": 300000.00
}
```

#### 4.5 Delete Employee
```http
DELETE {{BASE_URL}}/api/v1/employee/{{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

### 5. Payroll Management APIs

#### 5.1 Create Payroll
```http
POST {{BASE_URL}}/api/v1/payroll/create
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "payPeriodStart": "2024-08-01",
    "payPeriodEnd": "2024-08-31",
    "grossSalary": 250000.00,
    "netSalary": 220000.00,
    "paymentDate": "2024-09-01",
    "payStatus": "PENDING"
}
```

#### 5.2 Add Employee to Payroll
```http
POST {{BASE_URL}}/api/v1/payroll/add-employee/{{PAYROLL_ID}}?employeeId={{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.3 Remove Employee from Payroll
```http
POST {{BASE_URL}}/api/v1/payroll/remove-employee/{{PAYROLL_ID}}?employeeId={{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.4 Get Payroll by ID
```http
GET {{BASE_URL}}/api/v1/payroll/{{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.5 Get All Payrolls
```http
GET {{BASE_URL}}/api/v1/payroll/all
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.6 Update Payroll
```http
PUT {{BASE_URL}}/api/v1/payroll/update/{{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "payPeriodStart": "2024-08-01",
    "payPeriodEnd": "2024-08-31",
    "grossSalary": 275000.00,
    "netSalary": 240000.00,
    "paymentDate": "2024-09-01"
}
```

#### 5.7 Run Payroll
```http
POST {{BASE_URL}}/api/v1/payroll/run/{{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.8 Get Employees by Payroll ID
```http
GET {{BASE_URL}}/api/v1/payroll/employees/{{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 5.9 Delete Payroll
```http
DELETE {{BASE_URL}}/api/v1/payroll/delete/{{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

### 6. Payment Processing APIs

#### 6.1 Pay Individual Employee
```http
POST {{BASE_URL}}/api/v1/account/pay?employeeId={{EMPLOYEE_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 6.2 Pay Employees in Bulk
```http
POST {{BASE_URL}}/api/v1/account/pay/bulk?payrollId={{PAYROLL_ID}}
Authorization: Bearer {{JWT_TOKEN}}
```

#### 6.3 Get Bank Names
```http
GET {{BASE_URL}}/api/v1/account/banks
Authorization: Bearer {{JWT_TOKEN}}
```

#### 6.4 Verify Payment
```http
GET {{BASE_URL}}/api/v1/account/verify-pay/{{REFERENCE_NUMBER}}
Authorization: Bearer {{JWT_TOKEN}}
```

### 7. Virtual Account APIs

#### 7.1 Create Virtual Account
```http
POST {{BASE_URL}}/api/v1/virtual-account/create
Authorization: Bearer {{JWT_TOKEN}}
```

#### 7.2 Get Virtual Account Details
```http
GET {{BASE_URL}}/api/v1/virtual-account/details
Authorization: Bearer {{JWT_TOKEN}}
```

#### 7.3 Get Account Balance
```http
GET {{BASE_URL}}/api/v1/virtual-account/balance
Authorization: Bearer {{JWT_TOKEN}}
```

#### 7.4 Process Bank Transfer
```http
POST {{BASE_URL}}/api/v1/virtual-account/transfer?amount=100000
Authorization: Bearer {{JWT_TOKEN}}
```

#### 7.5 Fund Account via Card
```http
POST {{BASE_URL}}/api/v1/virtual-account/fund-card
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "amount": 50000.00,
    "currency": "NGN",
    "cardNumber": "5123450000000008",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123",
    "pin": "1234"
}
```

#### 7.6 Get Transaction History
```http
GET {{BASE_URL}}/api/v1/virtual-account/transactions?page=1&limit=10
Authorization: Bearer {{JWT_TOKEN}}
```

#### 7.7 Add Card to Account
```http
POST {{BASE_URL}}/api/v1/virtual-account/card/add
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
    "cardNumber": "5123450000000008",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123",
    "cardHolderName": "John Doe"
}
```

#### 7.8 Delete Card
```http
DELETE {{BASE_URL}}/api/v1/virtual-account/card/delete?cardId=1
Authorization: Bearer {{JWT_TOKEN}}
```

### 8. USSD Service APIs

#### 8.1 USSD Callback
```http
POST {{BASE_URL}}/api/v1/ussd
Content-Type: application/x-www-form-urlencoded

sessionId=123456789&serviceCode=*123#&phoneNumber=2349012345678&text=1
```

### 9. Webhook Endpoint

#### 9.1 KoraPay Webhook
```http
POST {{BASE_URL}}/webhook
Content-Type: application/json
X-Korapay-Signature: webhook_signature_here

{
    "event": "charge.success",
    "data": {
        "reference": "TXN_123456789",
        "amount": 50000,
        "currency": "NGN",
        "status": "success"
    }
}
```

## 🔧 Postman Collection Import

To import these into Postman:

1. **Create New Collection**: "PayCraft Backend API"
2. **Set Environment Variables**: Create environment with the variables listed above
3. **Add Authentication**: Set Bearer Token at collection level using `{{JWT_TOKEN}}`
4. **Copy Endpoints**: Add each endpoint as a new request in appropriate folders

## 📝 Testing Workflow

### Recommended Testing Order:
1. **Create Employer** → Save `employerId`
2. **Login** → Save `JWT_TOKEN`
3. **Create Company** → Save `companyId`
4. **Create Employee** → Save `employeeId`
5. **Create Virtual Account**
6. **Fund Virtual Account**
7. **Create Payroll** → Save `payrollId`
8. **Add Employee to Payroll**
9. **Run Payroll**
10. **Verify Payment Status**

## 🔍 Response Status Codes

- **200**: Success
- **201**: Created
- **400**: Bad Request (Validation errors)
- **401**: Unauthorized (Invalid/expired token)
- **403**: Forbidden
- **404**: Not Found
- **500**: Internal Server Error

## 📧 Sample Error Response
```json
{
    "statusCode": "400",
    "statusMessage": "Validation Failed",
    "data": {
        "emailAddress": "Email address is required",
        "password": "Password must be at least 8 characters"
    }
}
```

---

**Note**: Replace `{{BASE_URL}}` with your actual server URL and ensure all required environment variables are set in Postman before testing.