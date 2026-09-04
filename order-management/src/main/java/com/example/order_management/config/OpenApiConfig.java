package com.example.order_management.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger UI Configuration.
 *
 * Configures the interactive API documentation available at
 * /swagger-ui/index.html,
 * including API metadata and global JWT Bearer authentication scheme.
 */
@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "BearerAuth";

        /**
         * Builds and registers the OpenAPI definition bean.
         *
         * @return custom OpenAPI configuration
         */
        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                // 1. General API Metadata (Title, Description, Version, Developer Contact)
                                .info(new Info()
                                                .title("Order Management & Digital Wallet API")
                                                .description("REST API documentation for the Order Management and Digital Wallet System, featuring JWT authentication, wallet operations, product catalog, atomic order placement, and concurrency management.")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("Nikhil")
                                                                .email("nikhil@example.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://springdoc.org")))

                                // 2. Attach global security requirement so all protected endpoints show the
                                // padlock icon in Swagger UI
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                                // 3. Define the JWT Bearer Security Scheme (adds the green 'Authorize' button
                                // in Swagger UI)
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                                                .name(SECURITY_SCHEME_NAME)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Enter your JWT Bearer token: `Bearer <token>`")));
        }
}
