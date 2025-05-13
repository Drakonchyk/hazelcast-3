package com.example.facadeservice.service;

import java.time.Duration;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MessagesServiceCaller {

    private final WebClient.Builder wb;

    public MessagesServiceCaller(WebClient.Builder wb) {
        this.wb = wb;
    }

    /** POST one message */
    public void send(String body) {
        wb.build()
                .post()
                .uri("http://messages-service/messages")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
    }

    /** GET all messages */
    public List<String> findAll() {
        return wb.build()
                .get()
                .uri("http://messages-service/messages")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block(Duration.ofSeconds(5));
    }
}
