// facade-service/src/main/java/com/example/facadeservice/service/LoggingServiceCaller.java
package com.example.facadeservice.service;

import java.time.Duration;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LoggingServiceCaller {

    private final WebClient.Builder wb;

    public LoggingServiceCaller(WebClient.Builder wb) { this.wb = wb; }

    /** POST one log line to logging-service */
    public void send(String body) {
        wb.build()
                .post()
                .uri("http://logging-service/logs")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
    }

    /** GET all logs */
    public Map<String,String> fetchLogs() {
        return wb.build()
                .get()
                .uri("http://logging-service/logs")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,String>>() {})
                .block(Duration.ofSeconds(5));
    }
}
