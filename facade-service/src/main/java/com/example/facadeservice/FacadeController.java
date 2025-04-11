package com.example.facadeservice.controller;

import com.example.facadeservice.dto.MessageDto;
import com.example.facadeservice.service.LoggingServiceCaller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facade")
public class FacadeController {

    private final LoggingServiceCaller loggingCaller;

    public FacadeController(LoggingServiceCaller loggingCaller) {
        this.loggingCaller = loggingCaller;
    }

    @PostMapping("/messages")
    public String sendMessage(@RequestBody MessageDto msgDto) {
        return loggingCaller.sendMessage(msgDto);
    }

    @GetMapping("/messages")
    public String readAllMessages() {
        return loggingCaller.getAllMessages();
    }
}
