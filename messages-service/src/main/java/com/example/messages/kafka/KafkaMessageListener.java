package com.example.messages.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.example.messages.model.InMemoryStorage;

@Component
public class KafkaMessageListener {

    private final InMemoryStorage storage;

    public KafkaMessageListener(InMemoryStorage storage) {
        this.storage = storage;
    }

    @KafkaListener(topics = "message-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(String msg) {
        storage.add(msg);
        System.out.println("Consumed: " + msg);
    }
}
