package com.tdis.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    public static final String HEADER_ID = "X-User-Id";
    public static final String HEADER_TIPO = "X-User-Type";

    private final SecretKey secretKey;
    private final ReglasAcceso reglasAcceso;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret, ReglasAcceso reglasAcceso) {
        super(Config.class);
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.reglasAcceso = reglasAcceso;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            String path = request.getURI().getPath();
            HttpMethod metodo = request.getMethod();

            if (HttpMethod.OPTIONS.equals(metodo)
                    || path.equals("/api/auth/login")
                    || path.startsWith("/api/auth/register")) {
                // Rutas públicas: igual quitamos cualquier X-User-* que mande el cliente
                var limpia = request.mutate().headers(h -> {
                    h.remove(HEADER_ID);
                    h.remove(HEADER_TIPO);
                }).build();
                return chain.filter(exchange.mutate().request(limpia).build());
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado"));
            }

            Claims claims;
            try {
                claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(authHeader.substring(7))
                        .getPayload();
            } catch (Exception e) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido"));
            }

            String userId = claims.getSubject();
            String tipo = claims.get("tipo", String.class);

            // ¿Este rol puede usar este endpoint?
            if (!reglasAcceso.permite(metodo, path, tipo)) {
                log.warn("Acceso denegado: {} {} para rol {}", metodo, path, tipo);
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta accion"));
            }

            // Reemplazamos (no agregamos) los encabezados: si el cliente mandó un
            // X-User-Id falso, se borra y se pone el que viene en el token.
            var mutada = request.mutate().headers(h -> {
                h.remove(HEADER_ID);
                h.remove(HEADER_TIPO);
                h.set(HEADER_ID, userId);
                h.set(HEADER_TIPO, tipo);
            }).build();

            return chain.filter(exchange.mutate().request(mutada).build());
        };
    }

    public static class Config {
    }
}
