package com.orderflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 Configuration for OrderFlow SaaS Backend.
 * Configures Swagger UI with both JWT Bearer Token and API-Key security schemes.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "OrderFlow SaaS Backend API",
        version = "1.0.0",
        description = "Production-ready SaaS backend for WhatsApp automation, multi-tenant authentication, RBAC, and conversation management.",
        contact = @Contact(
            name = "OrderFlow Engineering Team",
            email = "support@orderflow.com"
        ),
        license = @License(
            name = "Proprietary",
            url = "https://orderflow.com/terms"
        )
    ),
    servers = {
        @Server(url = "/api/v1", description = "Current Server Path")
    },
    security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "apiKeyAuth")
    }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT access token"
    ),
    @SecurityScheme(
        name = "apiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key",
        description = "API key required for machine-to-machine REST endpoints"
    )
})
public class OpenApiConfig {
}
