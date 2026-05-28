package com.AgroLink.ProyectoAngular.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger UI / OpenAPI 3.0
 * Acceso en producción (Render): https://<tu-app>.onrender.com/swagger-ui.html
 * Acceso local:                  http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    /** URL de producción inyectada por variable de entorno (opcional) */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI agrolinkOpenAPI() {
        return new OpenAPI()
                // ── Información general ──────────────────────────────────────
                .info(new Info()
                        .title("AgroLink API")
                        .description("""
                                ## Plataforma AgroLink - Backend Spring Boot
                                
                                API REST para la gestión agrícola de AgroLink.
                                
                                ### Roles disponibles
                                | Rol           | Descripción                                      |
                                |---------------|--------------------------------------------------|
                                | ADMINISTRADOR | Gestión total de usuarios, validaciones           |
                                | AGRICULTOR    | Cultivos, lotes, eventos de producción            |
                                | COMPRADOR     | Catálogo, pedidos                                 |
                                
                                ### Autenticación
                                1. Usa **POST /api/auth/login** con tus credenciales.
                                2. Copia el `token` de la respuesta.
                                3. Haz clic en **Authorize** (🔒) e ingresa: `Bearer <token>`
                                
                                ### Usuarios de prueba (password: `Admin123!`)
                                | Email                        | Rol           |
                                |------------------------------|---------------|
                                | admin@agrolink.com           | ADMINISTRADOR |
                                | agricultor2@agrolink.com     | AGRICULTOR    |
                                | comprador@agrolink.com       | COMPRADOR     |
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Grupo 4 - Desarrollo Web Integrado")
                                .email("admin@agrolink.com"))
                        .license(new License()
                                .name("Uso académico")
                                .url("https://github.com")))

                // ── Servidores (local + producción) ──────────────────────────
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Servidor activo (local o Render)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Desarrollo local")
                ))

                // ── Seguridad JWT global ──────────────────────────────────────
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido en /api/auth/login")));
    }
}