package com.example.facadeservice.controller;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import com.example.facadeservice.service.LoggingServiceCaller;
import com.example.facadeservice.service.MessagesServiceCaller;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class FacadeController {

    private final LoggingServiceCaller logging;
    private final MessagesServiceCaller messages;
    private final DiscoveryClient discovery;
    private final WebClient.Builder wb;

    public FacadeController(LoggingServiceCaller logging,
                            MessagesServiceCaller messages,
                            DiscoveryClient discovery,
                            WebClient.Builder wb) {
        this.logging   = logging;
        this.messages  = messages;
        this.discovery = discovery;
        this.wb        = wb;
    }

    /* ---------- write APIs (unchanged) ---------- */

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public void writeLog(@RequestBody String body) { logging.send(body); }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void writeMsg(@RequestBody String body) { messages.send(body); }

    /* ---------- single-instance fetch (unchanged) ---------- */

    @GetMapping("/logs")
    public Map<String,String> logs() { return logging.fetchLogs(); }

    @GetMapping("/messages")
    public List<String> msgs() { return messages.findAll(); }

    /* ---------- NEW: aggregate over *all* replicas ---------- */

    @GetMapping("/all_logs")
    public Map<String,String> allLogs() {
        return discovery.getInstances("logging-service").stream()
                .map(this::fetchLogsFrom)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1             // in case of duplicate keys
                ));
    }

    @GetMapping("/all")
    public Map<String,Object> all() {
        Map<String,Object> result = new HashMap<>();
        result.put("logs",     allLogs());
        result.put("messages", allMessages());
        return result;
    }

    /* ---------- helpers ---------- */

    private List<String> allMessages() {
        return discovery.getInstances("messages-service").stream()
                .map(this::fetchMsgsFrom)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private final WebClient plain = WebClient.create();   // <-- add one field

    /* fetch /logs from one instance */
    private Map<String,String> fetchLogsFrom(ServiceInstance inst) {
        try {
            return plain.get()                          // <-- use plain client
                    .uri(inst.getUri() + "/logs")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String,String>>() {})
                    .block(Duration.ofSeconds(5));
        } catch (Exception ex) {
            return Map.of();                            // dead node → skip
        }
    }

    /* fetch /messages from one instance */
    private List<String> fetchMsgsFrom(ServiceInstance inst) {
        try {
            return plain.get()                         // ← plain, NOT wb.build()
                    .uri(inst.getUri() + "/messages")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block(Duration.ofSeconds(5));
        } catch (Exception ex) {
            return List.of();                          // unreachable/failed → skip
        }
    }
}
