package com.tdis.gateway.gcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

/**
 * Antes de reenviar una petición a un servicio interno:
 *  - quita cualquier X-Serverless-Authorization que mande el cliente, y
 *  - en producción (tdis.gcp.auth=true) agrega un ID token de Google para que
 *    Cloud Run deje pasar la llamada al servicio privado.
 *
 * El Authorization del usuario (su JWT) no se toca.
 */
@Component
public class GoogleIdTokenGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Serverless-Authorization";

    private final boolean habilitado;

    public GoogleIdTokenGlobalFilter(@Value("${tdis.gcp.auth:false}") boolean habilitado) {
        this.habilitado = habilitado;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI destino = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

        if (!habilitado || destino == null || !"https".equals(destino.getScheme())) {
            var limpia = exchange.getRequest().mutate().headers(h -> h.remove(HEADER)).build();
            return chain.filter(exchange.mutate().request(limpia).build());
        }

        String audience = destino.getScheme() + "://" + destino.getHost();
        // Pedir el token es una llamada HTTP bloqueante: se hace fuera del hilo reactivo
        return Mono.fromCallable(() -> GoogleIdTokens.obtener(audience))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(token -> {
                    var conToken = exchange.getRequest().mutate()
                            .headers(h -> h.set(HEADER, "Bearer " + token))
                            .build();
                    return chain.filter(exchange.mutate().request(conToken).build());
                });
    }

    /** Justo después de que el gateway decidió la URL destino. */
    @Override
    public int getOrder() {
        return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
    }
}
