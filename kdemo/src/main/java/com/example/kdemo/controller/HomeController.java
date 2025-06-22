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
        endpoints.put("kubernetes_pods", "/api/kubernetes/pods?namespace={namespace}&cluster={cluster}");
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
        endpoints.put("gpus_list", "/api/kubernetes/gpus?namespace={namespace}&cluster={cluster}");
        endpoints.put("gpu_get", "/api/kubernetes/gpus/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("gpu_create", "POST /api/kubernetes/gpus?namespace={namespace}&cluster={cluster}");
        endpoints.put("gpu_update", "PUT /api/kubernetes/gpus/{name}?namespace={namespace}&cluster={cluster}");
        endpoints.put("gpu_delete", "DELETE /api/kubernetes/gpus/{name}?namespace={namespace}&cluster={cluster}");
        
        // Prometheus 接口（查询参数形式）
        endpoints.put("prometheus_health", "/api/prometheus/health?cluster={cluster}");
        endpoints.put("prometheus_version", "/api/prometheus/version?cluster={cluster}");
        endpoints.put("prometheus_templates", "/api/prometheus/templates");
        endpoints.put("prometheus_query", "/api/prometheus/query?query={query}&time={time}&cluster={cluster}");
        endpoints.put("prometheus_query_range", "/api/prometheus/query-range?query={query}&start={start}&end={end}&step={step}&cluster={cluster}");
        endpoints.put("prometheus_batch_query", "POST /api/prometheus/batch-query?cluster={cluster}");
        endpoints.put("prometheus_batch_query_range", "POST /api/prometheus/batch-query-range?cluster={cluster}");
        
        // Prometheus 接口（路径参数形式 - 新的 RESTful 结构）
        endpoints.put("prometheus_health_cluster", "/api/clusters/{cluster}/prometheus/health");
        endpoints.put("prometheus_version_cluster", "/api/clusters/{cluster}/prometheus/version");
        endpoints.put("prometheus_query_cluster", "/api/clusters/{cluster}/prometheus/query?query={query}&time={time}");
        endpoints.put("prometheus_query_range_cluster", "/api/clusters/{cluster}/prometheus/query-range?query={query}&start={start}&end={end}&step={step}");
        endpoints.put("prometheus_batch_query_cluster", "POST /api/clusters/{cluster}/prometheus/batch-query");
        endpoints.put("prometheus_batch_query_range_cluster", "POST /api/clusters/{cluster}/prometheus/batch-query-range");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to K8s Demo Application");
        response.put("version", "2.5.0");
        response.put("type_safe", true);
        response.put("multi_cluster", true);
        response.put("default_cluster", "cluster-local");
        response.put("description", "A Spring Boot application with Kubernetes OpenAPI integration supporting multiple clusters, GPU resources, and Prometheus batch queries with hierarchical RESTful paths");
        response.put("supported_resources", new String[]{"Application", "Microservice", "GPU"});
        response.put("supported_apis", new String[]{"Kubernetes", "Prometheus"});
        
        Map<String, String> apiVersions = new HashMap<>();
        apiVersions.put("kubernetes", "v1");
        apiVersions.put("prometheus", "v1");
        response.put("api_versions", apiVersions);
        
        Map<String, String> pathStructure = new HashMap<>();
        pathStructure.put("kubernetes", "/api/kubernetes/*");
        pathStructure.put("prometheus_query_param", "/api/prometheus/*?cluster={cluster}");
        pathStructure.put("prometheus_path_param", "/api/clusters/{cluster}/prometheus/*");
        response.put("path_structure", pathStructure);
        
        response.put("endpoints", endpoints);
        
        return response;
    }
} 