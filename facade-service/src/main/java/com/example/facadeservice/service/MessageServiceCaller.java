package com.example.facadeservice.service;

import com.example.facadeservice.dto.ServiceInstanceDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class MessageServiceCaller {

    private final KafkaTemplate<String, String> kafka;
    private final ConfigServiceClient configClient;
    private final WebClient web = WebClient.create();
    private final Random rnd = new Random();

    public MessageServiceCaller(KafkaTemplate<String, String> kafka,
                                ConfigServiceClient configClient) {
        this.kafka = kafka;
        this.configClient = configClient;
    }

    /** Відправка в Kafka */
    public void sendMessage(String msg) {
        kafka.send("message-topic", msg);
    }

    /** Отримати List<String> з одного messages-service */
    public List<String> getAllMessages() {
        List<ServiceInstanceDto> instances = configClient.getMessageServiceInstances();
        Collections.shuffle(instances, rnd);

        for (ServiceInstanceDto inst : instances) {
            String url = "http://" + inst.getHost() + ":" + inst.getPort() + "/messages";
            try {
                return web.get()
                        .uri(url)
                        .retrieve()
                        // <-- ПАРСИМО JSON-масив у List<String>
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                        .block(Duration.ofSeconds(5));
            } catch (Exception e) {
                // якщо цей інстанс недоступний — спробуємо наступний
            }
        }
        throw new RuntimeException("No messages-service available");
    }
}
