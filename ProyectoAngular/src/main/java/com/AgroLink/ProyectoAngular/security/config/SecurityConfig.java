package com.AgroLink.ProyectoAngular.security.config;

import com.AgroLink.ProyectoAngular.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Rutas públicas ──────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Formulario público de contacto (landing page): cualquiera puede
                // enviar una solicitud, incluso sin cuenta. Listar/atender es solo ADMIN.
                .requestMatchers(HttpMethod.POST, "/api/solicitudes-contacto").permitAll()
                .requestMatchers(HttpMethod.GET,   "/api/solicitudes-contacto/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/solicitudes-contacto/**").hasRole("ADMINISTRADOR")

                // ── Swagger UI y OpenAPI docs (sin autenticación) ───────────
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml"
                ).permitAll()

                // ── Rutas protegidas por rol ────────────────────────────────
                .requestMatchers(HttpMethod.GET,   "/api/usuarios").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/*/validacion").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/pendientes").hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.POST,   "/api/cultivos").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PUT,    "/api/cultivos/**").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/cultivos/**").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PATCH,  "/api/cultivos/*/seguimiento").hasRole("AGRICULTOR")

                .requestMatchers(HttpMethod.POST,   "/api/eventos-produccion").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PUT,    "/api/eventos-produccion/**").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos-produccion/**").hasRole("AGRICULTOR")

                // ── RF04 / RF09 – Lotes comerciales ────────────────────────
                // IMPORTANTE: las rutas específicas públicas deben ir ANTES del comodín /**
                // También se permite OPTIONS para el preflight de CORS
                .requestMatchers(HttpMethod.OPTIONS, "/api/lotes/publicados/buscar").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/lotes/publicados").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/lotes/publicados/buscar").permitAll()
                // El comodín /** va después para no bloquear las rutas públicas anteriores
                .requestMatchers(HttpMethod.GET,    "/api/lotes/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/lotes/publicar").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.POST,   "/api/lotes").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PUT,    "/api/lotes/**").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/lotes/**").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PATCH,  "/api/lotes/*/stock").hasRole("AGRICULTOR")
                // Los endpoints /confirmar y /cancelar de lotes se eliminaron: el stock
                // solo se mueve internamente desde PedidoService (ver LoteController).
                .requestMatchers(HttpMethod.GET,  "/api/movimientos-stock/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/movimientos-stock").hasRole("AGRICULTOR")

                // -- RF07/RF08/RF12/RF18 - Pedidos ----------------------------
                // El compradorId/agricultorId ya NO se toma del cliente (ver PedidoController):
                // siempre se resuelve desde el usuario autenticado, y las consultas por
                // comprador/agricultor validan que el {id} de la URL sea el propio usuario
                // (o un ADMINISTRADOR). El control de rol aquí es una primera barrera.
                .requestMatchers(HttpMethod.POST,  "/api/pedidos").hasRole("COMPRADOR")
                .requestMatchers(HttpMethod.GET,   "/api/pedidos/comprador/**").authenticated()
                .requestMatchers(HttpMethod.GET,   "/api/pedidos/agricultor/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/confirmar").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/rechazar").hasRole("AGRICULTOR")
                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").authenticated()
                .requestMatchers(HttpMethod.GET,   "/api/pedidos/*/historial").authenticated()
                .requestMatchers(HttpMethod.GET,   "/api/pedidos/**").authenticated()
                .requestMatchers("/api/auditorias/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/notificaciones/**").authenticated()
                .requestMatchers("/api/reportes/**").authenticated()
                .requestMatchers("/api/backups/**").hasRole("ADMINISTRADOR")

                // ── Datos de contacto: las rutas específicas van ANTES del comodín
                // /** (igual que con /api/lotes más arriba), porque gana la primera
                // regla que matchea. El listado completo (direcciones, teléfonos,
                // emails de TODOS los usuarios) es solo para ADMINISTRADOR. El detalle
                // de contacto ligado a un pedido (RF11) valida propiedad en el controller.
                .requestMatchers(HttpMethod.GET, "/api/datos-contacto/pedido/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/datos-contacto/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/datos-contacto/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/datos-contacto").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/datos-contacto/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/lotes/*/precio").hasAnyRole("AGRICULTOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET,   "/api/lotes/*/precio-historial").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}