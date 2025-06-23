package com.example.kdemo.config;

public class K8sClusterConfig {
    private String name;
    private String apiServer;
    private String token;
    private String caCert;
    // getter/setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiServer() { return apiServer; }
    public void setApiServer(String apiServer) { this.apiServer = apiServer; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getCaCert() { return caCert; }
    public void setCaCert(String caCert) { this.caCert = caCert; }
} 