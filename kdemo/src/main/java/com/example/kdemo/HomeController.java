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
                "description", "A Spring Boot application with Kubernetes OpenAPI integration supporting multiple clusters",
                "default_cluster", "cluster-local",
                "endpoints", Map.ofEntries(
                        Map.entry("kubernetes_connection", "/api/kubernetes/connection?cluster={cluster}"),
                        Map.entry("kubernetes_namespaces", "/api/kubernetes/namespaces?cluster={cluster}"),
                        Map.entry("kubernetes_pods", "/api/kubernetes/namespaces/{namespace}/pods?cluster={cluster}"),
                        Map.entry("kubernetes_overview", "/api/kubernetes/overview?cluster={cluster}"),
                        Map.entry("crd_list", "/api/kubernetes/crds?cluster={cluster}"),
                        Map.entry("crd_detail", "/api/kubernetes/crds/{name}?cluster={cluster}"),
                        Map.entry("crd_create", "POST /api/kubernetes/crds?cluster={cluster}"),
                        Map.entry("applications_list", "/api/kubernetes/applications?namespace={namespace}&cluster={cluster}"),
                        Map.entry("application_get", "/api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}"),
                        Map.entry("application_create", "POST /api/kubernetes/applications?namespace={namespace}&cluster={cluster}"),
                        Map.entry("application_update", "PUT /api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}"),
                        Map.entry("application_delete", "DELETE /api/kubernetes/applications/{name}?namespace={namespace}&cluster={cluster}"),
                        Map.entry("microservices_list", "/api/kubernetes/microservices?namespace={namespace}&cluster={cluster}"),
                        Map.entry("microservice_get", "/api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}"),
                        Map.entry("microservice_create", "POST /api/kubernetes/microservices?namespace={namespace}&cluster={cluster}"),
                        Map.entry("microservice_update", "PUT /api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}"),
                        Map.entry("microservice_delete", "DELETE /api/kubernetes/microservices/{name}?namespace={namespace}&cluster={cluster}")
                ),
                "version", "2.1.0",
                "type_safe", true,
                "multi_cluster", true
        );
    }
} 