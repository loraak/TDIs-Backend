package com.tdis.gateway.config;

import com.tdis.gateway.security.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-open", r -> r
                .path("/api/auth/login")
                .filters(f -> f
                    .removeRequestHeader("Authorization"))
                .uri("lb://usuarios-service"))
            .route("auth-register-open", r -> r
                .path("/api/auth/register")
                .filters(f -> f
                    .removeRequestHeader("Authorization"))
                .uri("lb://usuarios-service"))
            .route("auth-register-externo-open", r -> r
                .path("/api/auth/register-externo")
                .filters(f -> f
                    .removeRequestHeader("Authorization"))
                .uri("lb://usuarios-service"))
            .route("auth-register-interno-open", r -> r
                .path("/api/auth/register-interno")
                .filters(f -> f
                    .removeRequestHeader("Authorization"))
                .uri("lb://usuarios-service"))
            .route("usuarios-service", r -> r
                .path("/api/auth/**", "/api/usuarios/**", "/api/admin/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri("lb://usuarios-service"))
            .route("catalogo-service", r -> r
                .path("/api/catalogo/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri("lb://catalogo-service"))
            .route("tramites-service", r -> r
                .path("/api/solicitudes/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri("lb://tramites-service"))
            .route("progreso-service", r -> r
                .path("/api/progreso/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri("lb://progreso-service"))
            .route("documentos-service", r -> r
                .path("/api/documentos/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri("lb://documentos-service"))
            .build();
    }
}