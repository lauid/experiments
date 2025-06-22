package com.example.kdemo.util;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.ApplicationSpec;
import com.example.kdemo.model.ApplicationStatus;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.MicroserviceSpec;
import com.example.kdemo.model.MicroserviceStatus;
import com.example.kdemo.model.GPU;
import com.example.kdemo.model.GPUSpec;
import com.example.kdemo.model.GPUStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试数据构建工具类
 * 用于创建各种测试用的Kubernetes资源对象
 */
public class TestDataBuilder {

    /**
     * 创建测试用的Application对象
     */
    public static Application createTestApplication(String name, String namespace) {
        Application application = new Application();
        
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        metadata.setNamespace(namespace);
        application.setMetadata(metadata);
        
        ApplicationSpec spec = new ApplicationSpec();
        spec.setName("test-app");
        spec.setVersion("v1.0.0");
        spec.setReplicas(3);
        application.setSpec(spec);
        
        ApplicationStatus status = new ApplicationStatus();
        status.setPhase("Running");
        application.setStatus(status);
        
        return application;
    }

    /**
     * 创建测试用的Microservice对象
     */
    public static Microservice createTestMicroservice(String name, String namespace) {
        Microservice microservice = new Microservice();
        
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        metadata.setNamespace(namespace);
        microservice.setMetadata(metadata);
        
        MicroserviceSpec spec = new MicroserviceSpec();
        spec.setName("test-service");
        spec.setVersion("v1.0.0");
        spec.setReplicas(2);
        
        Map<String, Object> envMap = new HashMap<>();
        envMap.put("ENV", "production");
        envMap.put("LOG_LEVEL", "INFO");
        
        spec.setEnvironment(java.util.Arrays.asList(envMap));
        
        microservice.setSpec(spec);
        
        MicroserviceStatus status = new MicroserviceStatus();
        status.setPhase("Running");
        microservice.setStatus(status);
        
        return microservice;
    }

    /**
     * 创建测试用的GPU对象
     */
    public static GPU createTestGPU(String name, String namespace) {
        GPU gpu = new GPU();
        
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);
        metadata.setNamespace(namespace);
        gpu.setMetadata(metadata);
        
        GPUSpec spec = new GPUSpec();
        spec.setModel("RTX 4090");
        spec.setComputeCapability("8.9");
        spec.setArchitecture("Ada Lovelace");
        spec.setStatus("available");
        spec.setNodeName("gpu-node-01");
        spec.setDriverVersion("535.86.10");
        spec.setCudaVersion("12.2");
        
        Map<String, String> memory = new HashMap<>();
        memory.put("total", "24GB");
        memory.put("available", "20GB");
        spec.setMemory(memory);
        
        Map<String, Integer> powerLimit = new HashMap<>();
        powerLimit.put("max", 450);
        powerLimit.put("current", 350);
        spec.setPowerLimit(powerLimit);
        
        Map<String, Integer> temperature = new HashMap<>();
        temperature.put("current", 65);
        temperature.put("max", 88);
        spec.setTemperature(temperature);
        
        Map<String, Integer> utilization = new HashMap<>();
        utilization.put("gpu", 45);
        utilization.put("memory", 60);
        spec.setUtilization(utilization);
        
        gpu.setSpec(spec);
        
        GPUStatus status = new GPUStatus();
        status.setPhase("Running");
        gpu.setStatus(status);
        
        return gpu;
    }

    /**
     * 创建Application的JSON字符串
     */
    public static String createApplicationJson(String name, String namespace) {
        return String.format("""
            {
                "apiVersion": "example.com/v1",
                "kind": "Application",
                "metadata": {
                    "name": "%s",
                    "namespace": "%s"
                },
                "spec": {
                    "name": "test-app",
                    "version": "v1.0.0",
                    "replicas": 3
                },
                "status": {
                    "phase": "Running"
                }
            }
            """, name, namespace);
    }

    /**
     * 创建Microservice的JSON字符串
     */
    public static String createMicroserviceJson(String name, String namespace) {
        return String.format("""
            {
                "apiVersion": "example.com/v1",
                "kind": "Microservice",
                "metadata": {
                    "name": "%s",
                    "namespace": "%s"
                },
                "spec": {
                    "name": "test-service",
                    "version": "v1.0.0",
                    "replicas": 2,
                    "environment": [
                        {
                            "ENV": "production",
                            "LOG_LEVEL": "INFO"
                        }
                    ]
                },
                "status": {
                    "phase": "Running"
                }
            }
            """, name, namespace);
    }

    /**
     * 创建GPU的JSON字符串
     */
    public static String createGPUJson(String name, String namespace) {
        return String.format("""
            {
                "apiVersion": "example.com/v1",
                "kind": "GPU",
                "metadata": {
                    "name": "%s",
                    "namespace": "%s"
                },
                "spec": {
                    "model": "RTX 4090",
                    "computeCapability": "8.9",
                    "architecture": "Ada Lovelace",
                    "status": "available",
                    "nodeName": "gpu-node-01",
                    "driverVersion": "535.86.10",
                    "cudaVersion": "12.2",
                    "memory": {
                        "total": "24GB",
                        "available": "20GB"
                    },
                    "powerLimit": {
                        "max": 450,
                        "current": 350
                    },
                    "temperature": {
                        "current": 65,
                        "max": 88
                    },
                    "utilization": {
                        "gpu": 45,
                        "memory": 60
                    }
                },
                "status": {
                    "phase": "Running"
                }
            }
            """, name, namespace);
    }
} 