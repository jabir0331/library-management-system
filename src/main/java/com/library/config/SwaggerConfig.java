package com.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    /** Every endpoint except login requires a token, so document the 401 once instead of per method. */
    @Bean
    public OpenApiCustomizer unauthorizedResponseCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, item) -> {
            if (path.equals("/api/auth/login")) {
                return;
            }
            item.readOperations().forEach(operation -> operation.getResponses().computeIfAbsent("401",
                    code -> new ApiResponse().description("Missing, invalid, expired or revoked token")));
        });
    }

    @Bean
    public OpenAPI libraryManagementOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info()
                        .title("Library Management System API")
                        .description("REST API for managing library members, books and editions, and the borrowing cycle (issue, return, fines, lost books). Authenticate with POST /api/auth/login, then use the token via the Authorize button.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nihmath Jabir")
                                .url("https://github.com/jabir0331/library-management-system")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}
