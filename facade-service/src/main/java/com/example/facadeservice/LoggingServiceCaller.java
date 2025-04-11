package com.example.facadeservice.service;

import com.example.facadeservice.dto.ServiceInstanceDto;
import com.example.facadeservice.dto.MessageDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class LoggingServiceCaller {

    private final ConfigServiceClient configClient;
    private final WebClient webClient;
    private final Random random;

    public LoggingServiceCaller(ConfigServiceClient configClient) {
        this.configClient = configClient;
        this.webClient = WebClient.builder().build();
        this.random = new Random();
    }

    public String sendMessage(MessageDto message) {
        // Отримати список екземплярів
        List<ServiceInstanceDto> instances = configClient.getLoggingServiceInstances();
        Collections.shuffle(instances); // перемішати
        // Цикл по інстансам, пробуємо викликати доки не вдасться
        for (ServiceInstanceDto inst : instances) {
            String url = "http://" + inst.getHost() + ":" + inst.getPort() + "/log";
            try {
                String response = webClient.post()
                        .uri(url)
                        .bodyValue(message)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(3)); // таймаут 3 сек
                return response; // Якщо успішно
            } catch (Exception e) {
                System.out.println("Instance " + url + " not available. Trying next...");
            }
        }
        throw new RuntimeException("No available logging-service instances.");
    }

    public String getAllMessages() {
        List<ServiceInstanceDto> instances = configClient.getLoggingServiceInstances();
        Collections.shuffle(instances);
        for (ServiceInstanceDto inst : instances) {
            String url = "http://" + inst.getHost() + ":" + inst.getPort() + "/log";
            try {
                String jsonResponse = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(3));
                return jsonResponse;
            } catch (Exception e) {
                System.out.println("Instance " + url + " not available. Trying next...");
            }
        }
        throw new RuntimeException("No available logging-service instances.");
    }
}
