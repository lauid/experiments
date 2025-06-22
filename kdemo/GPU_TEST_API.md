# GPU 接口测试记录

## 1. 获取GPU列表

**请求**
```
GET /api/kubernetes/gpus?namespace=default
```

**响应示例**
```
{
  "cluster": "cluster-local",
  "namespace": "default",
  "count": 2,
  "resources": [
    {
      "apiVersion": "example.com/v1",
      "kind": "GPU",
      "metadata": { ... },
      "spec": { ... },
      "status": { ... }
    },
    ...
  ]
}
```

---

## 2. 创建GPU

**请求**
```
POST /api/kubernetes/gpus?namespace=default
Content-Type: application/json
```
**请求体**
```
{
  "apiVersion": "example.com/v1",
  "kind": "GPU",
  "metadata": {
    "name": "rtx-4080-01",
    "namespace": "default",
    "labels": {
      "gpu-type": "gaming",
      "manufacturer": "nvidia"
    }
  },
  "spec": {
    "model": "RTX 4080",
    "memory": { "total": "16GB", "available": "14GB" },
    "computeCapability": "8.9",
    "architecture": "Ada Lovelace",
    "powerLimit": { "max": 320, "current": 280 },
    "temperature": { "current": 60, "max": 88 },
    "utilization": { "gpu": 30, "memory": 40 },
    "status": "available",
    "nodeName": "gpu-node-02",
    "driverVersion": "535.86.10",
    "cudaVersion": "12.2"
  },
  "status": {
    "phase": "Running",
    "conditions": [
      {
        "type": "Ready",
        "status": "True",
        "lastTransitionTime": "2024-01-15T10:30:00Z",
        "reason": "GPUAvailable",
        "message": "GPU is ready for use"
      }
    ],
    "lastUpdated": "2024-01-15T10:30:00Z"
  }
}
```
**响应示例**
```
{
  "success": true,
  "cluster": "cluster-local",
  "name": "rtx-4080-01",
  "message": "GPU created successfully"
}
```

---

## 3. 更新GPU

**请求**
```
PUT /api/kubernetes/gpus/rtx-4090-01?namespace=default
Content-Type: application/json
```
**请求体**
```
{
  "apiVersion": "example.com/v1",
  "kind": "GPU",
  "metadata": {
    "name": "rtx-4090-01",
    "namespace": "default",
    "labels": {
      "gpu-type": "gaming",
      "manufacturer": "nvidia",
      "updated": "true"
    },
    "resourceVersion": "17395"
  },
  "spec": {
    "model": "RTX 4090",
    "memory": { "total": "24GB", "available": "18GB" },
    "computeCapability": "8.9",
    "architecture": "Ada Lovelace",
    "powerLimit": { "max": 450, "current": 400 },
    "temperature": { "current": 70, "max": 88 },
    "utilization": { "gpu": 60, "memory": 75 },
    "status": "in-use",
    "nodeName": "gpu-node-01",
    "driverVersion": "535.86.10",
    "cudaVersion": "12.2"
  },
  "status": {
    "phase": "Running",
    "conditions": [
      {
        "type": "Ready",
        "status": "True",
        "lastTransitionTime": "2024-01-15T10:30:00Z",
        "reason": "GPUAvailable",
        "message": "GPU is ready for use"
      }
    ],
    "lastUpdated": "2024-01-15T11:00:00Z"
  }
}
```
**响应示例**
```
{
  "success": true,
  "cluster": "cluster-local",
  "name": "rtx-4090-01",
  "message": "GPU updated successfully"
}
```

---

## 4. 再次获取GPU列表

**请求**
```
GET /api/kubernetes/gpus?namespace=default
```
**响应示例**
```
{
  "cluster": "cluster-local",
  "namespace": "default",
  "count": 2,
  "resources": [ ... ]
}
``` 