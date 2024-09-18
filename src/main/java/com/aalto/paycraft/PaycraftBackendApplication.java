package com.aalto.paycraft;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(
        info = @Info(
            title = "PayCraft Documentation",
            description = "PayCraft REST API Documentation",
            version = "v1",
            contact = @Contact(
                    name = "PayCraft",
                    url = "https://github.com/orgs/PayCraft-NG"
            )
        ),
        externalDocs =  @ExternalDocumentation(
                description = "PayCraft REST API Documentation",
                url = "https://github.com/PayCraft-NG/PayCraft-Backend.git"
        )
)
public class PaycraftBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaycraftBackendApplication.class, args);
    }
}
