package com.example.kdemo.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
} 