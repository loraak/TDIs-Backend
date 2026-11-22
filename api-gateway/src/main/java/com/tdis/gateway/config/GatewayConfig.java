package com.tdis.gateway.config;

import com.tdis.gateway.security.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /*
     * Destino de cada servicio:
     *   local (dev):  lb://nombre-servicio  -> se resuelve con Eureka
     *   producción:   https://xxx.run.app   -> URL de Cloud Run (variables *_URL)
     */
    @Value("${tdis.url.usuarios}")
    private String usuariosUrl;
    @Value("${tdis.url.catalogo}")
    private String catalogoUrl;
    @Value("${tdis.url.tramites}")
    private String tramitesUrl;
    @Value("${tdis.url.progreso}")
    private String progresoUrl;
    @Value("${tdis.url.documentos}")
    private String documentosUrl;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-open", r -> r
                .path("/api/auth/login")
                .filters(f -> f
                    .removeRequestHeader("Authorization")
                    .removeRequestHeader(JwtAuthFilter.HEADER_ID)
                    .removeRequestHeader(JwtAuthFilter.HEADER_TIPO))
                .uri(usuariosUrl))
            .route("auth-register-open", r -> r
                .path("/api/auth/register")
                .filters(f -> f
                    .removeRequestHeader("Authorization")
                    .removeRequestHeader(JwtAuthFilter.HEADER_ID)
                    .removeRequestHeader(JwtAuthFilter.HEADER_TIPO))
                .uri(usuariosUrl))
            .route("auth-register-externo-open", r -> r
                .path("/api/auth/register-externo")
                .filters(f -> f
                    .removeRequestHeader("Authorization")
                    .removeRequestHeader(JwtAuthFilter.HEADER_ID)
                    .removeRequestHeader(JwtAuthFilter.HEADER_TIPO))
                .uri(usuariosUrl))
            .route("auth-register-interno-open", r -> r
                .path("/api/auth/register-interno")
                .filters(f -> f
                    .removeRequestHeader("Authorization")
                    .removeRequestHeader(JwtAuthFilter.HEADER_ID)
                    .removeRequestHeader(JwtAuthFilter.HEADER_TIPO))
                .uri(usuariosUrl))
            .route("usuarios-service", r -> r
                .path("/api/auth/**", "/api/usuarios/**", "/api/admin/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(usuariosUrl))
            .route("catalogo-service", r -> r
                .path("/api/catalogo/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(catalogoUrl))
            .route("tramites-service", r -> r
                .path("/api/solicitudes/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(tramitesUrl))
            .route("progreso-service", r -> r
                .path("/api/progreso/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(progresoUrl))
            .route("documentos-service", r -> r
                .path("/api/documentos/**")
                .filters(f -> f
                    .filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(documentosUrl))
            .build();
    }
}