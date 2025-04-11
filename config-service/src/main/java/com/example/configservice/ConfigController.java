package com.example.configservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
public class ConfigController {

    @GetMapping("/services/logging-service")
    public List<ServiceInstance> getLoggingServices() {
        return Arrays.asList(
            new ServiceInstance("localhost", 8081),
            new ServiceInstance("localhost", 8082),
            new ServiceInstance("localhost", 8083)
        );
    }
    
}
