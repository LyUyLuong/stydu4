package com.lul.Stydu4.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${application.swagger.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${application.swagger.prod-url:https://api.stydu4.com}")
    private String prodUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define development server
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Development Server");

        // Define production server
        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Production Server");

        // Define contact information
        Contact contact = new Contact();
        contact.setName("Stydu4 Support Team");
        contact.setEmail("support@stydu4.com");
        contact.setUrl("https://www.stydu4.com");

        // Define license
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // Define API information
        Info info = new Info()
                .title("Stydu4 Learning Management System API")
                .version("1.0.0")
                .contact(contact)
                .description("RESTful API documentation for Stydu4 - A comprehensive platform for managing exams, tests, questions, users, roles, and permissions. " +
                        "This API supports features including user authentication (JWT & OAuth2), file uploads, exam management, and role-based access control.")
                .termsOfService("https://www.stydu4.com/terms")
                .license(mitLicense);

        // Define security scheme for JWT Bearer token
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter your JWT token in the format: Bearer {token}");

        // Define security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuthScheme))
                .addSecurityItem(securityRequirement);
    }
}
