package com.example.facadeservice.service;

import com.example.facadeservice.dto.ServiceInstanceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ConfigServiceClient {

    private final WebClient webClient;
    private final String configServerUrl;

    public ConfigServiceClient(@Value("${config.server.url}") String configServerUrl) {
        this.webClient = WebClient.builder().build();
        this.configServerUrl = configServerUrl;
    }

    public List<ServiceInstanceDto> getLoggingServiceInstances() {
        return webClient
            .get()
            .uri(configServerUrl + "/services/logging-service")
            .retrieve()
            .bodyToFlux(ServiceInstanceDto.class)
            .collectList()
            .block();
    }
    public List<ServiceInstanceDto> getMessageServiceInstances() {
        return webClient
                .get()
                .uri(configServerUrl + "/services/messages-service")
                .retrieve()
                .bodyToFlux(ServiceInstanceDto.class)
                .collectList()
                .block();
    }
}
