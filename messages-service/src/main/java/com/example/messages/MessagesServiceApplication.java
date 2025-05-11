package com.example.messages;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.IOException;
import java.net.ServerSocket;

@SpringBootApplication
public class MessagesServiceApplication {

    public static void main(String[] args) {
        // choose a free port in [8091…8092]
        int port = findFirstFreePort();
        new SpringApplicationBuilder(MessagesServiceApplication.class)
                .properties("server.port=" + port)
                .run(args);
        System.out.println("Started messages-service on port " + port);
    }

    private static int findFirstFreePort() {
        for (int port = 8091; port <= 8092; port++) {
            try (ServerSocket sock = new ServerSocket(port)) {
                sock.setReuseAddress(true);
                return port;
            } catch (IOException e) {
                // in use, next
            }
        }
        throw new IllegalStateException(
                "No free port in range " + 8091 + "–" + 8092);
    }
}
