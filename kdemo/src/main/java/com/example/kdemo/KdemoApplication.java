package com.example.kdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.kubernetes.client.openapi.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.kdemo.model.Vendor;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class KdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KdemoApplication.class, args);
	}
}
