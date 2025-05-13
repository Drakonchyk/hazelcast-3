package com.example.loggingservice.config;

import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Value("${hazelcast.tcp.members}")
    private List<String> members;      // comma-separated list from Consul KV

    @Bean
    public Config customHazelcastConfig() {
        Config cfg = new Config().setClusterName("log-cluster");

        JoinConfig join = cfg.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true)
                .setMembers(members);

        return cfg;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config customHazelcastConfig) {
        return Hazelcast.newHazelcastInstance(customHazelcastConfig);
    }
}
