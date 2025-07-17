package com.example.kdemo.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.kdemo.model.Vendor;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import io.kubernetes.client.openapi.JSON;

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

    @PostConstruct
    public void customizeK8sGson() {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Vendor.class, new Vendor.Adapter())
            .registerTypeAdapter(java.time.OffsetDateTime.class, new com.google.gson.TypeAdapter<java.time.OffsetDateTime>() {
                @Override
                public void write(com.google.gson.stream.JsonWriter out, java.time.OffsetDateTime value) throws java.io.IOException {
                    out.value(value == null ? null : value.toString());
                }
                @Override
                public java.time.OffsetDateTime read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                    String s = in.nextString();
                    return java.time.OffsetDateTime.parse(s);
                }
            })
            .create();
        JSON.setGson(gson);
    }
} 