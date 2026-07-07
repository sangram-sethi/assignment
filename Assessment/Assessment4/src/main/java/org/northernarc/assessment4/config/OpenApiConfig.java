package org.northernarc.assessment4.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI 3 definition for the Secure Banking API.
 *
 * <p>Declares a global {@code bearerAuth} security scheme so the generated
 * Swagger UI exposes an "Authorize" button for pasting a JWT obtained from
 * {@code POST /api/auth/login}. Documentation is served at
 * {@code /swagger-ui.html} and the raw contract at {@code /v3/api-docs}.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Secure Banking API",
                version = "1.0.0",
                description = "REST API for customer, account and transaction management "
                        + "with JWT authentication and role-based authorization.",
                contact = @Contact(name = "Northern Arc Assessment"),
                license = @License(name = "Proprietary")),
        servers = @Server(url = "/", description = "Default server"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT bearer token obtained from POST /api/auth/login")
public class OpenApiConfig {
}
