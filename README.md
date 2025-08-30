# PayCraft Backend 💼

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **A comprehensive payroll management system designed to streamline employee payment processing for businesses of all sizes.**

## 🌟 Features

### Core Functionality
- 👥 **Employer Management** - Complete profile management with authentication
- 🏢 **Company Management** - Multi-company support with detailed business profiles
- 👨‍💼 **Employee Management** - Comprehensive employee data with bank details
- 💰 **Payroll Processing** - Automated and manual payroll runs
- 🏦 **Virtual Account Integration** - Seamless payment processing via KoraPay
- 📱 **USSD Support** - Offline access for basic operations
- 🔔 **Real-time Notifications** - Payment status updates and system alerts

### Security & Performance
- 🔐 **JWT Authentication** - Secure token-based authentication
- 🛡️ **Role-based Authorization** - Granular access control
- 🔒 **Data Encryption** - Sensitive data protection
- ⚡ **Optimized Performance** - Sub-2 second response times
- 📊 **Audit Logging** - Comprehensive activity tracking

## 🚀 Tech Stack

**Backend Framework:**
- Java 17
- Spring Boot 3.3.3
- Spring Security 6
- Spring Data JPA
- Spring Validation

**Database:**
- MySQL 8.0 (Production)
- H2 Database (Testing)

**External Services:**
- KoraPay API (Payment Processing)
- Email Service (Notifications)

**DevOps & Tools:**
- Docker & Docker Compose
- Maven (Build Tool)
- Swagger/OpenAPI (API Documentation)
- Lombok (Code Generation)

## 📋 Prerequisites

Before running this application, make sure you have:

- ☕ **Java 17** or higher
- 🔧 **Maven 3.6+**
- 🐳 **Docker** (optional, for containerized deployment)
- 🗄️ **MySQL 8.0** (for production setup)

## ⚡ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/PayCraft-NG/PayCraft-Backend.git
cd PayCraft-Backend
```

### 2. Environment Configuration
Create a `.env` file in the root directory:

```bash
# Database Configuration
DATABASE_NAME=paycraft_db
DATABASE_USERNAME=your_db_username
DATABASE_DEV_PASSWORD=your_dev_password
DATABASE_PROD_PASSWORD=your_prod_password

# Security
SECRET_STRING=your_jwt_secret_key_here
ENCRYPTION_KEY=your_32_character_encryption_key

# KoraPay Configuration
KORA_SECRET_KEY=your_kora_secret_key
KORA_PUBLIC_KEY=your_kora_public_key

# Email Configuration
EMAIL_SENDER=your_email@gmail.com
EMAIL_PASSWORD=your_email_app_password

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Webhook URLs
WEBHOOK_URL=https://your-domain.com
WEBHOOK_DEV_URL=http://localhost:6020/webhook/
```

### 3. Database Setup

**Option A: Using MySQL (Recommended - Default)**
```bash
# Create database
mysql -u root -p
CREATE DATABASE paycraft_db;
GRANT ALL PRIVILEGES ON paycraft_db.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;

# Run with default dev profile
mvn spring-boot:run
```

**Option B: Using H2 (Quick Setup)**
```bash
# Set profile to qa for H2 database
export SPRING_PROFILES_ACTIVE=qa
mvn spring-boot:run
```

### 4. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/paycraft-0.0.1-SNAPSHOT.jar
```

### 5. Docker Deployment (Optional)

```bash
# Build Docker image
./build_script.sh

# Or manually
docker build -t paycraft-backend:latest .
docker run -p 6020:6020 paycraft-backend:latest
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: `http://localhost:6020/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:6020/v3/api-docs`

### Authentication
Most endpoints require JWT authentication. Include the Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## 🌐 API Endpoints Overview

| Endpoint Category | Base URL | Description |
|------------------|----------|-------------|
| Authentication | `/api/v1/auth` | Login, token refresh |
| Employer Management | `/api/v1/employer` | CRUD operations for employers |
| Company Management | `/api/v1/company` | Company profile management |
| Employee Management | `/api/v1/employee` | Employee profile management |
| Payroll Management | `/api/v1/payroll` | Payroll creation and processing |
| Payment Processing | `/api/v1/account` | Payment operations |
| Virtual Account | `/api/v1/virtual-account` | Account funding and management |
| Webhooks | `/webhook` | External service notifications |
| USSD Service | `/api/v1/ussd` | Offline access endpoints |

## 📊 Project Structure

```
src/
├── main/
│   ├── java/com/aalto/paycraft/
│   │   ├── api/           # USSD controllers
│   │   ├── audit/         # Auditing configuration
│   │   ├── config/        # Security & app configuration
│   │   ├── constants/     # Application constants
│   │   ├── controller/    # REST controllers
│   │   ├── dto/           # Data Transfer Objects
│   │   ├── entity/        # JPA entities
│   │   ├── exception/     # Custom exceptions
│   │   ├── mapper/        # Entity-DTO mappers
│   │   ├── repository/    # Data access layer
│   │   └── service/       # Business logic layer
│   └── resources/
│       ├── schema/        # Database schemas
│       ├── templates/     # Email templates
│       └── application*.yml # Configuration files
└── test/                  # Test classes
```

## 🔧 Configuration Profiles

- **`dev`** - MySQL database for development (Default)
- **`qa`** - H2 in-memory database for quick testing
- **`prod`** - Production MySQL configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🔗 Related Projects
- [PayCraft Frontend](https://github.com/PayCraft-NG/PayCraft-Frontend) - React.js frontend application

**Built with ❤️ during a 2-week koraPay hackathon**

*PayCraft - Simplifying payroll management for the modern workplace*