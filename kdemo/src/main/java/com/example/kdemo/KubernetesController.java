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
     * 检查 Kubernetes 连接
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> checkConnection(@RequestParam(required = false) String cluster) {
        boolean connected = kubernetesService.isConnected(cluster);
        Map<String, Object> response = new HashMap<>();
        response.put("connected", connected);
        response.put("cluster", cluster != null ? cluster : "cluster-local");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有命名空间
     */
    @GetMapping("/namespaces")
    public ResponseEntity<Map<String, Object>> getNamespaces(@RequestParam(required = false) String cluster) {
        try {
            List<String> namespaces = kubernetesService.getNamespaces(cluster);
            Map<String, Object> response = new HashMap<>();
            response.put("namespaces", namespaces);
            response.put("count", namespaces.size());
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get namespaces");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    @GetMapping("/namespaces/{namespace}/pods")
    public ResponseEntity<Map<String, Object>> getPodsInNamespace(
            @PathVariable String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            List<String> pods = kubernetesService.getPodsInNamespace(cluster, namespace);
            Map<String, Object> response = new HashMap<>();
            response.put("namespace", namespace);
            response.put("pods", pods);
            response.put("count", pods.size());
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get pods in namespace: " + namespace);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取集群概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getClusterOverview(@RequestParam(required = false) String cluster) {
        try {
            List<String> namespaces = kubernetesService.getNamespaces(cluster);
            Map<String, Object> response = new HashMap<>();
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            response.put("namespaces", namespaces);
            response.put("namespaceCount", namespaces.size());
            
            // 获取每个命名空间的Pod数量
            Map<String, Integer> podsPerNamespace = new HashMap<>();
            for (String namespace : namespaces) {
                try {
                    List<String> pods = kubernetesService.getPodsInNamespace(cluster, namespace);
                    podsPerNamespace.put(namespace, pods.size());
                } catch (Exception e) {
                    podsPerNamespace.put(namespace, 0);
                }
            }
            response.put("podsPerNamespace", podsPerNamespace);
            
            int totalPods = podsPerNamespace.values().stream().mapToInt(Integer::intValue).sum();
            response.put("totalPods", totalPods);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get cluster overview");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== CRD 相关 API ==========

    /**
     * 获取所有CRD
     */
    @GetMapping("/crds")
    public ResponseEntity<Map<String, Object>> getCustomResourceDefinitions(@RequestParam(required = false) String cluster) {
        try {
            List<String> crds = kubernetesService.getCustomResourceDefinitions(cluster);
            Map<String, Object> response = new HashMap<>();
            response.put("crds", crds);
            response.put("count", crds.size());
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get CRDs");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 获取指定CRD的详细信息
     */
    @GetMapping("/crds/{name}")
    public ResponseEntity<Map<String, Object>> getCustomResourceDefinition(
            @PathVariable String name,
            @RequestParam(required = false) String cluster) {
        try {
            Map<String, Object> crd = kubernetesService.getCustomResourceDefinition(cluster, name);
            Map<String, Object> response = new HashMap<>();
            response.put("crd", crd);
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get CRD: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 创建CRD
     */
    @PostMapping("/crds")
    public ResponseEntity<Map<String, Object>> createCustomResourceDefinition(
            @RequestBody String crdYaml,
            @RequestParam(required = false) String cluster) {
        try {
            Map<String, Object> result = kubernetesService.createCustomResourceDefinition(cluster, crdYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create CRD");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== 类型安全的 Application 资源 API ==========

    /**
     * 获取所有 Application 资源
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplications(
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            List<Application> applications = kubernetesService.getApplications(cluster, namespace);
            Map<String, Object> response = new HashMap<>();
            response.put("namespace", namespace);
            response.put("applications", applications);
            response.put("count", applications.size());
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get applications");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
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
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            Application application = kubernetesService.getApplication(cluster, namespace, name);
            Map<String, Object> response = new HashMap<>();
            response.put("application", application);
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get application: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 创建 Application 资源
     */
    @PostMapping("/applications")
    public ResponseEntity<Map<String, Object>> createApplication(
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.createApplication(cluster, namespace, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create application");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 更新 Application 资源
     */
    @PutMapping("/applications/{name}")
    public ResponseEntity<Map<String, Object>> updateApplication(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.updateApplication(cluster, namespace, name, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update application: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 删除 Application 资源
     */
    @DeleteMapping("/applications/{name}")
    public ResponseEntity<Map<String, Object>> deleteApplication(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            Map<String, Object> result = kubernetesService.deleteApplication(cluster, namespace, name);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete application: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== 类型安全的 Microservice 资源 API ==========

    /**
     * 获取所有 Microservice 资源
     */
    @GetMapping("/microservices")
    public ResponseEntity<Map<String, Object>> getMicroservices(
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            List<Microservice> microservices = kubernetesService.getMicroservices(cluster, namespace);
            Map<String, Object> response = new HashMap<>();
            response.put("namespace", namespace);
            response.put("microservices", microservices);
            response.put("count", microservices.size());
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get microservices");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
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
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            Microservice microservice = kubernetesService.getMicroservice(cluster, namespace, name);
            Map<String, Object> response = new HashMap<>();
            response.put("microservice", microservice);
            response.put("cluster", cluster != null ? cluster : "cluster-local");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get microservice: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 创建 Microservice 资源
     */
    @PostMapping("/microservices")
    public ResponseEntity<Map<String, Object>> createMicroservice(
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.createMicroservice(cluster, namespace, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create microservice");
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 更新 Microservice 资源
     */
    @PutMapping("/microservices/{name}")
    public ResponseEntity<Map<String, Object>> updateMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster,
            @RequestBody String resourceYaml) {
        try {
            Map<String, Object> result = kubernetesService.updateMicroservice(cluster, namespace, name, resourceYaml);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update microservice: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 删除 Microservice 资源
     */
    @DeleteMapping("/microservices/{name}")
    public ResponseEntity<Map<String, Object>> deleteMicroservice(
            @PathVariable String name,
            @RequestParam(required = false) String namespace,
            @RequestParam(required = false) String cluster) {
        try {
            Map<String, Object> result = kubernetesService.deleteMicroservice(cluster, namespace, name);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete microservice: " + name);
            errorResponse.put("cluster", cluster != null ? cluster : "cluster-local");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 