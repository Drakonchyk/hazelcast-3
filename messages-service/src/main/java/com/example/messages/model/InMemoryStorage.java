package com.example.messages.model;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class InMemoryStorage {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());

    public void add(String msg) {
        messages.add(msg);
    }

    public List<String> getAll() {
        return new ArrayList<>(messages);
    }
}
