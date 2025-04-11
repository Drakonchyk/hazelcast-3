package com.example.configservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
public class ConfigController {

    // Приклад статичного списку екземплярів logging-service:
    // Можна буде змінювати host/port під час запуску чи конфігурації
    @GetMapping("/services/logging-service")
    public List<ServiceInstance> getLoggingServices() {
        return Arrays.asList(
            new ServiceInstance("localhost", 8081),
            new ServiceInstance("localhost", 8082),
            new ServiceInstance("localhost", 8083)
        );
    }

    // Тут можна зробити аналогічно для messages-service, якщо потрібно.
}
