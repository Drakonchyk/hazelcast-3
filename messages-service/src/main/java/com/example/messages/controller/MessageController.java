package com.example.messages.controller;

import com.example.messages.model.InMemoryStorage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final InMemoryStorage storage;

    public MessageController(InMemoryStorage storage) {
        this.storage = storage;
    }

    // GET http://host:port/messages
    @GetMapping("/messages")
    public List<String> getMessages() {
        return storage.getAll();
    }
}
