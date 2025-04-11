package com.example.loggingservice.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Bean
    public Config myHazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("logging-hazelcast-instance");

        // Налаштуємо, щоб ноди бачили одна одну (наприклад, через TCP/IP):
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        // Вимикаємо Multicast для спрощення
        join.getMulticastConfig().setEnabled(false);
        // Увімкнемо TCP/IP та додамо локальний вузол (можливо, localhost або інші адреси)
        join.getTcpIpConfig()
            .setEnabled(true)
            .addMember("127.0.0.1"); 
        return config;
    }

    @Bean
    public HazelcastInstance myHazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
