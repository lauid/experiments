package com.example.kdemo;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/kubernetes")
public class KubernetesController {

    private final KubernetesService kubernetesService;

    @Autowired
    public KubernetesController(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    /**
     * 检查 Kubernetes 连接状态
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> checkConnection() {
        boolean isConnected = kubernetesService.isConnected();
        return ResponseEntity.ok(Map.of(
                "connected", isConnected,
                "message", isConnected ? "Successfully connected to Kubernetes" : "Failed to connect to Kubernetes"
        ));
    }

    /**
     * 获取所有命名空间
     */
    @GetMapping("/namespaces")
    public ResponseEntity<Map<String, Object>> getNamespaces() {
        try {
            List<String> namespaces = kubernetesService.getNamespaces();
            return ResponseEntity.ok(Map.of(
                    "namespaces", namespaces,
                    "count", namespaces.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get namespaces",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    @GetMapping("/namespaces/{namespace}/pods")
    public ResponseEntity<Map<String, Object>> getPodsInNamespace(@PathVariable String namespace) {
        try {
            List<String> pods = kubernetesService.getPodsInNamespace(namespace);
            return ResponseEntity.ok(Map.of(
                    "namespace", namespace,
                    "pods", pods,
                    "count", pods.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get pods in namespace: " + namespace,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取集群信息概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getClusterOverview() {
        try {
            boolean isConnected = kubernetesService.isConnected();
            if (!isConnected) {
                return ResponseEntity.ok(Map.of(
                        "connected", false,
                        "message", "Not connected to Kubernetes cluster"
                ));
            }

            List<String> namespaces = kubernetesService.getNamespaces();
            int totalPods = 0;
            
            for (String namespace : namespaces) {
                List<String> pods = kubernetesService.getPodsInNamespace(namespace);
                totalPods += pods.size();
            }

            return ResponseEntity.ok(Map.of(
                    "connected", true,
                    "namespaces", namespaces.size(),
                    "totalPods", totalPods,
                    "namespaceList", namespaces
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get cluster overview",
                    "message", e.getMessage()
            ));
        }
    }

    // ========== CRD 相关 API ==========

    /**
     * 获取所有CRD
     */
    @GetMapping("/crds")
    public ResponseEntity<Map<String, Object>> getCustomResourceDefinitions() {
        try {
            List<String> crds = kubernetesService.getCustomResourceDefinitions();
            return ResponseEntity.ok(Map.of(
                    "crds", crds,
                    "count", crds.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get CRDs",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取指定CRD的详细信息
     */
    @GetMapping("/crds/{name}")
    public ResponseEntity<Map<String, Object>> getCustomResourceDefinition(@PathVariable String name) {
        try {
            Map<String, Object> crd = kubernetesService.getCustomResourceDefinition(name);
            return ResponseEntity.ok(crd);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get CRD: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 创建CRD
     */
    @PostMapping("/crds")
    public ResponseEntity<Map<String, Object>> createCustomResourceDefinition(@RequestBody String crdYaml) {
        try {
            Map<String, Object> result = kubernetesService.createCustomResourceDefinition(crdYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to create CRD",
                    "message", e.getMessage()
            ));
        }
    }

    // ========== 类型安全的 Application 资源 API ==========

    /**
     * 获取所有 Application 资源
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplications(@RequestParam(required = false) String namespace) {
        try {
            List<Application> applications = kubernetesService.getApplications(namespace);
            Map<String, Object> response = new HashMap<>();
            response.put("namespace", namespace);
            response.put("applications", applications);
            response.put("count", applications.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get applications");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取指定的 Application 资源
     */
    @GetMapping("/applications/{name}")
    public ResponseEntity<Map<String, Object>> getApplication(
            @PathVariable String name,
            @RequestParam(required = false) String namespace) {
        try {
            Application application = kubernetesService.getApplication(namespace, name);
            return ResponseEntity.ok(Map.of(
                    "application", application
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get application: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 创建 Application 资源
     */
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> createApplication(
            @RequestParam(required = false) String namespace,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.createApplication(namespace, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to create application",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 更新 Application 资源
     */
    @PutMapping("/applications/{name}")
    public ResponseEntity<Map<String, Object>> updateApplication(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.updateApplication(namespace, name, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to update application: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 删除 Application 资源
     */
    @DeleteMapping("/applications/{name}")
    public ResponseEntity<Map<String, Object>> deleteApplication(
            @PathVariable String name,
            @RequestParam(required = false) String namespace) {
        try {
            Map<String, Object> result = kubernetesService.deleteApplication(namespace, name);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to delete application: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    // ========== 类型安全的 Microservice 资源 API ==========

    /**
     * 获取所有 Microservice 资源
     */
    @GetMapping("/microservices")
    public ResponseEntity<Map<String, Object>> getMicroservices(@RequestParam(required = false) String namespace) {
        try {
            List<Microservice> microservices = kubernetesService.getMicroservices(namespace);
            Map<String, Object> response = new HashMap<>();
            response.put("namespace", namespace);
            response.put("microservices", microservices);
            response.put("count", microservices.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get microservices");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取指定的 Microservice 资源
     */
    @GetMapping("/microservices/{name}")
    public ResponseEntity<Map<String, Object>> getMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String namespace) {
        try {
            Microservice microservice = kubernetesService.getMicroservice(namespace, name);
            return ResponseEntity.ok(Map.of(
                    "microservice", microservice
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get microservice: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 创建 Microservice 资源
     */
    @PostMapping("/microservices")
    public ResponseEntity<Map<String, Object>> createMicroservice(
            @RequestParam(required = false) String namespace,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.createMicroservice(namespace, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to create microservice",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 更新 Microservice 资源
     */
    @PutMapping("/microservices/{name}")
    public ResponseEntity<Map<String, Object>> updateMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.updateMicroservice(namespace, name, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to update microservice: " + name,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 删除 Microservice 资源
     */
    @DeleteMapping("/microservices/{name}")
    public ResponseEntity<Map<String, Object>> deleteMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String namespace) {
        try {
            Map<String, Object> result = kubernetesService.deleteMicroservice(namespace, name);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to delete microservice: " + name,
                    "message", e.getMessage()
            ));
        }
    }
} 