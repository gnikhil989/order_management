# Guide 02: OpenAPI 3 & Swagger UI Setup

> **Purpose**: This guide focuses exclusively on **OpenAPI 3 (Swagger UI)** integration using **SpringDoc**. It explains how API documentation is generated, the annotations used, and how to test endpoints interactively in your browser.

---

## 1. What is OpenAPI 3 & SpringDoc?

* **OpenAPI Specification (OAS)**: An industry-standard format for describing REST APIs (paths, parameters, request/response bodies, HTTP codes, and authentication).
* **Swagger UI**: A visual, interactive web application that reads the OpenAPI specification and renders a browser dashboard to explore and test endpoints.
* **SpringDoc (`springdoc-openapi`)**: The official library for Spring Boot 3+ that inspects your `@RestController`, `@GetMapping`, `@PostMapping`, etc., and automatically generates the OpenAPI documentation in real time.

---

## 2. Maven Dependency

In [`pom.xml`](../pom.xml):

```xml
<!-- SpringDoc OpenAPI 3 UI Starter -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

---

## 3. OpenAPI Configuration Explained (`OpenApiConfig.java`)

File: [`src/main/java/com/example/order_management/config/OpenApiConfig.java`](../src/main/java/com/example/order_management/config/OpenApiConfig.java)

```java
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

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API general information
                .info(new Info()
                        .title("Order Management & Digital Wallet API")
                        .description("REST API documentation for the Order Management and Digital Wallet System.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nikhil")
                                .email("nikhil@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                
                // Add global security requirement (adds the lock icon to endpoints in UI)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                
                // Define the Bearer Authentication scheme (JWT)
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token: `Bearer <token>`")));
    }
}
```

### Key Components:
1. **`Info` Object**: Configures API title, version, description, and contact info displayed in the header banner of Swagger UI.
2. **`SecurityScheme`**: Configures the **"Authorize"** button on the UI so you can paste JWT tokens and test protected endpoints.
3. **`SecurityRequirement`**: Marks endpoints with a padlock icon indicating they require authentication.

---

## 4. Useful OpenAPI Annotations for Controllers & DTOs

| Annotation | Placement | What It Does |
|:---|:---|:---|
| `@Tag(name, description)` | Controller Class | Groups endpoints under a logical section (e.g., "Users", "Wallets"). |
| `@Operation(summary, description)` | Controller Method | Describes what the endpoint does in one line summary and detailed description. |
| `@ApiResponse(responseCode, description)` | Controller Method | Documents possible HTTP response codes (e.g., `200`, `400`, `404`, `422`). |
| `@Parameter(description, required)` | Method Arguments | Documents query parameters (`@RequestParam`) or path variables (`@PathVariable`). |
| `@Schema(description, example)` | DTO / Entity Fields | Documents JSON field requirements, types, and sample values. |

---

## 5. How to Access and Test in Browser

1. Run the Spring Boot application.
2. Open your browser:
   * **Swagger UI Dashboard**: `http://localhost:8080/swagger-ui/index.html`
   * **Raw OpenAPI JSON Data**: `http://localhost:8080/v3/api-docs`
3. Click on any endpoint (e.g., `GET /api/v1/health`), click **"Try it out"**, then **"Execute"** to see live responses.

---

## 6. Interview Questions & Production Best Practices

> **Question 1: How should Swagger UI and OpenAPI docs be handled in a production environment?**
> * **Answer**: "Exposing internal API schemas publicly in production introduces security reconnaissance risks. We either disable Swagger in production by setting `springdoc.swagger-ui.enabled=false` and `springdoc.api-docs.enabled=false` in `application-prod.properties`, or restrict access to the `/swagger-ui/**` and `/v3/api-docs/**` endpoints behind Spring Security requiring `ROLE_ADMIN` authentication or an internal VPN network."

> **Question 2: What is the difference between `@SecurityScheme` and `@SecurityRequirement` in OpenAPI 3?**
> * **Answer**: "`@SecurityScheme` defines the authentication mechanism globally (e.g., HTTP Bearer JWT, OAuth2, or API Key) and its metadata. `@SecurityRequirement` actually applies that scheme to individual controller classes or methods, signaling to Swagger UI to attach the Bearer token in the `Authorization` header when sending requests."

> **Question 3: Why is `@Schema(example = "...")` critical for frontend-backend contract collaboration?**
> * **Answer**: "Providing realistic field examples allows frontend developers to understand payload structures instantly without guessing format conventions (e.g., `"150.00"` for currency vs raw numbers). It also powers automated contract testing tools and mock API servers (like Prism) generated directly from the OpenAPI JSON specification."

> **Question 4: How does SpringDoc differ from older Springfox implementations in Spring Boot 3?**
> * **Answer**: "Springfox is unmaintained and incompatible with Spring Boot 3+ / Spring 6 (which migrated from `javax.*` to `jakarta.*` packages and reworked path matching). SpringDoc was built natively for OpenAPI 3 and Spring Boot 3, supporting reactive WebFlux, standard Java records, and modern Jackson serialization out of the box."

