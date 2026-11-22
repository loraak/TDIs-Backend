package com.tdis.gateway.gcp;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Obtiene ID tokens de Google desde el "metadata server" de Cloud Run.
 * Con ellos un servicio demuestra su identidad al llamar a otro servicio privado.
 *
 * Solo funciona dentro de Google Cloud. Los tokens duran 1 hora; se guardan en
 * caché 50 minutos por cada servicio destino (audience).
 */
public final class GoogleIdTokens {

    private static final String METADATA_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience=";
    private static final Duration VIGENCIA_CACHE = Duration.ofMinutes(50);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private record TokenEnCache(String token, Instant expira) {
    }

    private static final Map<String, TokenEnCache> CACHE = new ConcurrentHashMap<>();

    private GoogleIdTokens() {
    }

    /** audience = URL base del servicio destino, ej. https://catalogo-xxx.run.app */
    public static String obtener(String audience) {
        TokenEnCache enCache = CACHE.get(audience);
        if (enCache != null && Instant.now().isBefore(enCache.expira())) {
            return enCache.token();
        }
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(METADATA_URL + URLEncoder.encode(audience, StandardCharsets.UTF_8)))
                    .header("Metadata-Flavor", "Google")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new IllegalStateException("Metadata server respondio " + res.statusCode());
            }
            String token = res.body().trim();
            CACHE.put(audience, new TokenEnCache(token, Instant.now().plus(VIGENCIA_CACHE)));
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("No se pudo obtener ID token para " + audience, e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener ID token para " + audience, e);
        }
    }

    /** De "https://host/ruta?x=1" deja solo "https://host". */
    public static String audienceDe(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getHost();
    }
}
