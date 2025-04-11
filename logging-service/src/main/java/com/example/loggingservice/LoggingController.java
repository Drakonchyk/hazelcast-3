package com.example.loggingservice.controller;

import com.example.loggingservice.service.LoggingHazelcastService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/log")
public class LoggingController {

    private final LoggingHazelcastService loggingService;

    public LoggingController(LoggingHazelcastService loggingService) {
        this.loggingService = loggingService;
    }

    @PostMapping
    public String saveMessage(@RequestBody MessageDto dto) {
        // Генеруємо унікальний ключ. Можна брати поточний розмір + 1, або UUID
        String key = "msg-" + UUID.randomUUID();
        loggingService.storeMessage(key, dto.getMsg());
        System.out.println("[LoggingService] Received message: " + dto.getMsg());
        return "Message saved with key: " + key;
    }

    @GetMapping
    public Map<String, String> getMessages() {
        return loggingService.getAllMessages();
    }
}
