package com.example.facadeservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced               // enables discovery-based host resolution
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
