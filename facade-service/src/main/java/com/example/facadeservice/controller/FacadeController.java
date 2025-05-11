package com.example.facadeservice.controller;

import com.example.facadeservice.dto.MessageDto;
import com.example.facadeservice.service.LoggingServiceCaller;
import com.example.facadeservice.service.MessageServiceCaller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facade")
public class FacadeController {

    private final LoggingServiceCaller logCaller;
    private final MessageServiceCaller msgCaller;

    public FacadeController(LoggingServiceCaller logCaller,
                            MessageServiceCaller msgCaller) {
        this.logCaller = logCaller;
        this.msgCaller = msgCaller;
    }

    // ——— Logging ———
    @PostMapping("/logs")
    public ResponseEntity<String> sendLog(@RequestBody MessageDto dto) {
        String result = logCaller.sendMessage(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/logs")
    public ResponseEntity<Map<String,String>> readLogs() {
        return ResponseEntity.ok(logCaller.getAllMessages());
    }

    // ——— Messaging ———
    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDto dto) {
        msgCaller.sendMessage(dto.getMsg());
        return ResponseEntity.ok("Sent to Kafka");
    }

    @GetMapping("/messages")
    public ResponseEntity<List<String>> readMessages() {
        List<String> all = msgCaller.getAllMessages();
        return ResponseEntity.ok(all);
    }

    // ——— Combined ———
    @GetMapping("/all")
    public ResponseEntity<Map<String,Object>> all() {
        return ResponseEntity.ok(Map.of(
                "logs",     logCaller.getAllMessages(),
                "messages", msgCaller.getAllMessages()
        ));
    }
}
