package com.github.seecret1.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnClass(OpenAPI.class)
public class SwaggerConfig {

    @Value("${springdoc.server.url:http://localhost:${server.port:8080}}")
    private String serverUrl;

    @Value("${springdoc.server.description:Local Server}")
    private String serverDescription;

    @Value("${spring.application.name:Unknown Service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("API for " + applicationName)
                        .contact(new Contact()
                                .name("seecret1")
                                .email("support@bankapp.com")
                                .url("https://github.com/seecret1"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}