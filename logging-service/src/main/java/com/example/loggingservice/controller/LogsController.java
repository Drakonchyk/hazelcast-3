// logging-service/src/main/java/com/example/loggingservice/controller/LogsController.java
package com.example.loggingservice.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogsController {

    // naïve in-memory store, fine for a demo
    private final Map<String,String> store = new ConcurrentHashMap<>();

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody String message) {
        store.put("log-" + System.nanoTime(), message);
    }

    @GetMapping("/logs")
    public Map<String,String> all() {
        return store;
    }
}
