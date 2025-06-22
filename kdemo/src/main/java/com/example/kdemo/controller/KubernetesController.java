package com.example.kdemo.controller;

import com.example.kdemo.dto.*;
import com.example.kdemo.exception.KubernetesException;
import com.example.kdemo.exception.ResourceNotFoundException;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.service.KubernetesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/kubernetes")
public class KubernetesController {

    private final KubernetesService kubernetesService;

    @Autowired
    public KubernetesController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    // ========== 基础 Kubernetes 资源操作 ==========

    @GetMapping("/connection")
    public ResponseEntity<ClusterInfo> checkConnection(@RequestParam(required = false) String cluster) {
        ClusterInfo info = kubernetesService.checkConnection(cluster);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/namespaces")
    public ResponseEntity<NamespaceInfo> getNamespaces(@RequestParam(required = false) String cluster) {
        NamespaceInfo info = kubernetesService.getNamespaces(cluster);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/pods")
    public ResponseEntity<PodInfo> getPodsInNamespace(
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        PodInfo info = kubernetesService.getPodsInNamespace(cluster, namespace);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/overview")
    public ResponseEntity<ClusterOverview> getClusterOverview(@RequestParam(required = false) String cluster) {
        ClusterOverview overview = kubernetesService.getClusterOverview(cluster);
        return ResponseEntity.ok(overview);
    }

    // ========== CRD 操作 ==========

    @GetMapping("/crds")
    public ResponseEntity<CrdInfo> getCustomResourceDefinitions(@RequestParam(required = false) String cluster) {
        CrdInfo info = kubernetesService.getCustomResourceDefinitions(cluster);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/crds/{name}")
    public ResponseEntity<Map<String, Object>> getCustomResourceDefinition(
            @PathVariable String name,
            @RequestParam(required = false) String cluster) {
        try {
            Map<String, Object> crd = kubernetesService.getCustomResourceDefinition(cluster, name);
            return ResponseEntity.ok(crd);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/crds")
    public ResponseEntity<OperationResult> createCustomResourceDefinition(
            @RequestBody String crdYaml,
            @RequestParam(required = false) String cluster) {
        OperationResult result = kubernetesService.createCustomResourceDefinition(cluster, crdYaml);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ========== Application 资源操作 ==========

    @GetMapping("/applications")
    public ResponseEntity<ResourceResponse<Application>> getApplications(
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        ResourceResponse<Application> response = kubernetesService.getApplications(cluster, namespace);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/{name}")
    public ResponseEntity<Application> getApplication(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        try {
            Application application = kubernetesService.getApplication(cluster, namespace, name);
            return ResponseEntity.ok(application);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/applications")
    public ResponseEntity<OperationResult> createApplication(
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.createApplication(cluster, namespace, resourceYaml);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/applications/{name}")
    public ResponseEntity<OperationResult> updateApplication(
            @PathVariable String name,
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.updateApplication(cluster, namespace, name, resourceYaml);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/applications/{name}")
    public ResponseEntity<OperationResult> deleteApplication(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.deleteApplication(cluster, namespace, name);
        return ResponseEntity.ok(result);
    }

    // ========== Microservice 资源操作 ==========

    @GetMapping("/microservices")
    public ResponseEntity<ResourceResponse<Microservice>> getMicroservices(
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        ResourceResponse<Microservice> response = kubernetesService.getMicroservices(cluster, namespace);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/microservices/{name}")
    public ResponseEntity<Microservice> getMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        try {
            Microservice microservice = kubernetesService.getMicroservice(cluster, namespace, name);
            return ResponseEntity.ok(microservice);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/microservices")
    public ResponseEntity<OperationResult> createMicroservice(
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.createMicroservice(cluster, namespace, resourceYaml);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/microservices/{name}")
    public ResponseEntity<OperationResult> updateMicroservice(
            @PathVariable String name,
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.updateMicroservice(cluster, namespace, name, resourceYaml);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/microservices/{name}")
    public ResponseEntity<OperationResult> deleteMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.deleteMicroservice(cluster, namespace, name);
        return ResponseEntity.ok(result);
    }

    // ========== GPU 资源操作 ==========

    @GetMapping("/gpus")
    public ResponseEntity<ResourceResponse<GPU>> getGPUs(
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        ResourceResponse<GPU> response = kubernetesService.getGPUs(cluster, namespace);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/gpus/{name}")
    public ResponseEntity<GPU> getGPU(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        try {
            GPU gpu = kubernetesService.getGPU(cluster, namespace, name);
            return ResponseEntity.ok(gpu);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/gpus")
    public ResponseEntity<OperationResult> createGPU(
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.createGPU(cluster, namespace, resourceYaml);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/gpus/{name}")
    public ResponseEntity<OperationResult> updateGPU(
            @PathVariable String name,
            @RequestBody String resourceYaml,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.updateGPU(cluster, namespace, name, resourceYaml);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/gpus/{name}")
    public ResponseEntity<OperationResult> deleteGPU(
            @PathVariable String name,
            @RequestParam(required = false) String cluster,
            @RequestParam(defaultValue = "default") String namespace) {
        OperationResult result = kubernetesService.deleteGPU(cluster, namespace, name);
        return ResponseEntity.ok(result);
    }

    // ========== 异常处理 ==========

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(KubernetesException.class)
    public ResponseEntity<Map<String, String>> handleKubernetesException(KubernetesException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
} 