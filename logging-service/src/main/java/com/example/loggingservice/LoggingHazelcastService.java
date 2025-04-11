package com.example.loggingservice.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoggingHazelcastService {

    private static final String MESSAGES_MAP_NAME = "messages_map";
    private final HazelcastInstance hazelcastInstance;
    private IMap<String, String> messagesMap;

    public LoggingHazelcastService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostConstruct
    public void init() {
        messagesMap = hazelcastInstance.getMap(MESSAGES_MAP_NAME);
    }

    public void storeMessage(String key, String value) {
        messagesMap.put(key, value);
    }

    public Map<String, String> getAllMessages() {
        return messagesMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public int getSize() {
        return messagesMap.size();
    }
}
