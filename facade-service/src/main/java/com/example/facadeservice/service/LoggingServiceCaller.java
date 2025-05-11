package com.example.facadeservice.service;

import com.example.facadeservice.dto.ServiceInstanceDto;
import com.example.facadeservice.dto.MessageDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class LoggingServiceCaller {

    private final ConfigServiceClient configClient;
    private final WebClient webClient;
    private final Random random = new Random();

    public LoggingServiceCaller(ConfigServiceClient configClient) {
        this.configClient = configClient;
        this.webClient = WebClient.builder().build();
    }

    public String sendMessage(MessageDto message) {
        List<ServiceInstanceDto> instances = configClient.getLoggingServiceInstances();
        Collections.shuffle(instances);
        for (ServiceInstanceDto inst : instances) {
            String url = "http://" + inst.getHost() + ":" + inst.getPort() + "/log";
            try {
                return webClient.post()
                        .uri(url)
                        .bodyValue(message)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(3));
            } catch (Exception e) {
                // try next
            }
        }
        throw new RuntimeException("No available logging-service instances.");
    }

    public Map<String, String> getAllMessages() {
        List<ServiceInstanceDto> instances = configClient.getLoggingServiceInstances();
        Collections.shuffle(instances);
        for (ServiceInstanceDto inst : instances) {
            String url = "http://" + inst.getHost() + ":" + inst.getPort() + "/log";
            try {
                return webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                        .block(Duration.ofSeconds(3));
            } catch (Exception e) {
                // try next
            }
        }
        throw new RuntimeException("No available logging-service instances.");
    }
}
