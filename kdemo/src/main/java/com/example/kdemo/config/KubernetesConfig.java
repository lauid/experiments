package com.example.kdemo.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

@Configuration
public class KubernetesConfig {
    
    public static final String DEFAULT_CLUSTER = "cluster-local";
    
    @Bean
    public ApiClient kubernetesApiClient() {
        try {
            return ClientBuilder.standard().build();
        } catch (IOException e) {
            try {
                return ClientBuilder.cluster().build();
            } catch (IOException ioException) {
                return new ApiClient();
            }
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
} 