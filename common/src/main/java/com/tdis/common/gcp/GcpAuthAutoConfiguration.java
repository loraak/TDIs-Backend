package com.tdis.common.gcp;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * En producción (tdis.gcp.auth=true) cada llamada Feign a otro servicio lleva un
 * ID token de Google en X-Serverless-Authorization. Cloud Run lo valida antes de
 * dejar pasar la petición, así los servicios internos no aceptan llamadas de
 * nadie más. Se usa X-Serverless-Authorization para no chocar con Authorization.
 *
 * Se registra sola en todos los servicios que dependen de common
 * (ver META-INF/spring/...AutoConfiguration.imports).
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(name = "tdis.gcp.auth", havingValue = "true")
public class GcpAuthAutoConfiguration {

    public static final String HEADER = "X-Serverless-Authorization";

    @Bean
    public RequestInterceptor googleIdTokenInterceptor() {
        return template -> {
            String url = template.feignTarget().url();
            if (url == null || !url.startsWith("https://")) {
                return; // no es una URL de Cloud Run (ej. local con Eureka)
            }
            String token = GoogleIdTokens.obtener(GoogleIdTokens.audienceDe(url));
            template.header(HEADER, "Bearer " + token);
        };
    }
}
