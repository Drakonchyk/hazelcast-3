// messages-service/src/main/java/com/example/messagesservice/controller/MessagesController.java
package com.example.messages.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessagesController {

    private final List<String> mailbox = new CopyOnWriteArrayList<>();

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody String msg) {
        mailbox.add(msg);
    }

    @GetMapping("/messages")
    public List<String> all() {
        return mailbox;
    }
}
