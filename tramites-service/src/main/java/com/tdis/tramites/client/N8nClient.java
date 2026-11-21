package com.tdis.tramites.client;

import com.tdis.common.dto.AnalisisIAResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class N8nClient {

    private final RestTemplate restTemplate;

    @Value("${n8n.webhook.url}")
    private String webhookUrl;

    public N8nClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));   // tiempo máximo para conectar
        factory.setReadTimeout(Duration.ofSeconds(60));     // tiempo máximo esperando la respuesta
        this.restTemplate = new RestTemplate(factory);
    }

    public AnalisisIAResponse analizarEvidencia(String nombreActividad, String descripcion,
                                                 byte[] imagenBytes, String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("nombreActividad", nombreActividad);
        body.put("descripcion", descripcion != null ? descripcion : "");
        body.put("imagenBase64", Base64.getEncoder().encodeToString(imagenBytes));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<AnalisisIAResponse> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    request,
                    AnalisisIAResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al llamar n8n webhook: {}", e.getMessage());
            AnalisisIAResponse fallback = new AnalisisIAResponse();
            fallback.setEstado("Revision Humana");
            fallback.setMotivo("No fue posible conectar con el servicio de analisis IA.");
            fallback.setVeredicto_modelo(null);
            fallback.setFue_rechazado_por_modelo(false);
            return fallback;
        }
    }
}
