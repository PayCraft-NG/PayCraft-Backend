package com.aalto.paycraft.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PayCraft API",
                version = "v1.0.0",
                description = "REST API for PayCraft — a payroll management platform that handles employer " +
                        "onboarding, company and employee management, payroll processing, and virtual account " +
                        "funding via KoraPay. " +
                        "Public routes: employer signup, company create (with employerId), auth login/refresh, " +
                        "USSD callback, and KoraPay webhooks. All other routes require a Bearer JWT.",
                contact = @Contact(
                        name = "PayCraft",
                        url = "https://paycraft-dev.netlify.app"
                ),
                license = @License(name = "MIT License")
        ),
        servers = {
                @Server(url = "/", description = "Current environment")
        },
        externalDocs = @ExternalDocumentation(
                description = "PayCraft backend repository",
                url = "https://github.com/PayCraft-NG/PayCraft-Backend"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste the access token returned by POST /api/v1/auth/login. " +
                "Example: Bearer eyJhbGciOiJIUzI1NiJ9..."
)
public class OpenApiConfig {}
