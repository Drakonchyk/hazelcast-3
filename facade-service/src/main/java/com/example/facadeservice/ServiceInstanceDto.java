package com.example.facadeservice.dto;

public class ServiceInstanceDto {
    private String host;
    private int port;

    public ServiceInstanceDto() {}
    public ServiceInstanceDto(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
