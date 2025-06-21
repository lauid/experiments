package com.example.kdemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "message", "Welcome to K8s Demo Application",
                "description", "A Spring Boot application with Kubernetes OpenAPI integration",
                "endpoints", Map.ofEntries(
                        Map.entry("kubernetes_status", "/api/kubernetes/status"),
                        Map.entry("kubernetes_namespaces", "/api/kubernetes/namespaces"),
                        Map.entry("kubernetes_pods", "/api/kubernetes/namespaces/{namespace}/pods"),
                        Map.entry("kubernetes_overview", "/api/kubernetes/overview"),
                        Map.entry("crd_list", "/api/kubernetes/crds"),
                        Map.entry("crd_detail", "/api/kubernetes/crds/{name}"),
                        Map.entry("crd_create", "POST /api/kubernetes/crds"),
                        Map.entry("applications_list", "/api/kubernetes/applications"),
                        Map.entry("application_get", "/api/kubernetes/applications/{name}"),
                        Map.entry("application_create", "POST /api/kubernetes/applications"),
                        Map.entry("application_update", "PUT /api/kubernetes/applications/{name}"),
                        Map.entry("application_delete", "DELETE /api/kubernetes/applications/{name}"),
                        Map.entry("microservices_list", "/api/kubernetes/microservices"),
                        Map.entry("microservice_get", "/api/kubernetes/microservices/{name}"),
                        Map.entry("microservice_create", "POST /api/kubernetes/microservices"),
                        Map.entry("microservice_update", "PUT /api/kubernetes/microservices/{name}"),
                        Map.entry("microservice_delete", "DELETE /api/kubernetes/microservices/{name}")
                ),
                "version", "2.0.0",
                "type_safe", true
        );
    }
} 