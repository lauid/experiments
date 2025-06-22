package com.example.kdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("kubernetes_connection", "/api/kubernetes/connection?cluster={cluster}");
        endpoints.put("kubernetes_namespaces", "/api/kubernetes/namespaces?cluster={cluster}");
        endpoints.put("kubernetes_pods", "/api/kubernetes/namespaces/{namespace}/pods?cluster={cluster}");
        endpoints.put("kubernetes_overview", "/api/kubernetes/overview?cluster={cluster}");
        endpoints.put("crd_list", "/api/kubernetes/crds?cluster={cluster}");
        endpoints.put("crd_detail", "/api/kubernetes/crds/{name}?cluster={cluster}");
        endpoints.put("crd_create", "POST /api/kubernetes/crds?cluster={cluster}");
        endpoints.put("applications_list", "/api/kubernetes/applications?namespace={namespace}&cluster={cluster}");
        endpoints.put("application_get", "/api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("application_create", "POST /api/kubernetes/applications?namespace={namespace}&cluster={cluster}");
        endpoints.put("application_update", "PUT /api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("application_delete", "DELETE /api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("microservices_list", "/api/kubernetes/microservices?namespace={namespace}&cluster={cluster}");
        endpoints.put("microservice_get", "/api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("microservice_create", "POST /api/kubernetes/microservices?namespace={namespace}&cluster={cluster}");
        endpoints.put("microservice_update", "PUT /api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("microservice_delete", "DELETE /api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}");

        return Map.of(
                "message", "Welcome to K8s Demo Application",
                "version", "2.1.0",
                "type_safe", true,
                "multi_cluster", true,
                "default_cluster", "cluster-local",
                "description", "A Spring Boot application with Kubernetes OpenAPI integration supporting multiple clusters",
                "endpoints", endpoints
        );
    }
} 