# Multi-Cluster Kubernetes API Support

## 概述

K8s Demo Application 现在支持多集群操作，默认集群名为 `cluster-local`。所有 API 端点都支持可选的 `cluster` 参数来指定目标集群。

## 默认集群

- **默认集群名**: `cluster-local`
- **行为**: 如果不指定 `cluster` 参数，系统将使用默认集群

## API 端点

### 基础 Kubernetes 操作

#### 1. 连接检查
```bash
# 检查默认集群连接
GET /api/kubernetes/connection

# 检查指定集群连接
GET /api/kubernetes/connection?cluster=production-cluster
```

#### 2. 命名空间管理
```bash
# 获取默认集群的所有命名空间
GET /api/kubernetes/namespaces

# 获取指定集群的所有命名空间
GET /api/kubernetes/namespaces?cluster=staging-cluster
```

#### 3. Pod 管理
```bash
# 获取默认集群中指定命名空间的 Pod
GET /api/kubernetes/namespaces/default/pods

# 获取指定集群中指定命名空间的 Pod
GET /api/kubernetes/namespaces/default/pods?cluster=production-cluster
```

#### 4. 集群概览
```bash
# 获取默认集群概览
GET /api/kubernetes/overview

# 获取指定集群概览
GET /api/kubernetes/overview?cluster=staging-cluster
```

### CRD 管理

#### 1. 获取 CRD 列表
```bash
# 获取默认集群的 CRD 列表
GET /api/kubernetes/crds

# 获取指定集群的 CRD 列表
GET /api/kubernetes/crds?cluster=production-cluster
```

#### 2. 获取 CRD 详情
```bash
# 获取默认集群的 CRD 详情
GET /api/kubernetes/crds/applications.example.com

# 获取指定集群的 CRD 详情
GET /api/kubernetes/crds/applications.example.com?cluster=staging-cluster
```

#### 3. 创建 CRD
```bash
# 在默认集群创建 CRD
POST /api/kubernetes/crds
Content-Type: application/json

# 在指定集群创建 CRD
POST /api/kubernetes/crds?cluster=production-cluster
Content-Type: application/json
```

### Application 资源管理

#### 1. 获取 Application 列表
```bash
# 获取默认集群的所有 Application
GET /api/kubernetes/applications

# 获取指定集群的所有 Application
GET /api/kubernetes/applications?cluster=production-cluster

# 获取指定集群指定命名空间的 Application
GET /api/kubernetes/applications?namespace=default&cluster=staging-cluster
```

#### 2. 获取 Application 详情
```bash
# 获取默认集群的 Application
GET /api/kubernetes/applications/my-app

# 获取指定集群的 Application
GET /api/kubernetes/applications/my-app?cluster=production-cluster

# 获取指定集群指定命名空间的 Application
GET /api/kubernetes/applications/my-app?namespace=default&cluster=staging-cluster
```

#### 3. 创建 Application
```bash
# 在默认集群创建 Application
POST /api/kubernetes/applications
Content-Type: application/json

# 在指定集群创建 Application
POST /api/kubernetes/applications?cluster=production-cluster
Content-Type: application/json

# 在指定集群指定命名空间创建 Application
POST /api/kubernetes/applications?namespace=default&cluster=staging-cluster
Content-Type: application/json
```

#### 4. 更新 Application
```bash
# 更新默认集群的 Application
PUT /api/kubernetes/applications/my-app
Content-Type: application/json

# 更新指定集群的 Application
PUT /api/kubernetes/applications/my-app?cluster=production-cluster
Content-Type: application/json

# 更新指定集群指定命名空间的 Application
PUT /api/kubernetes/applications/my-app?namespace=default&cluster=staging-cluster
Content-Type: application/json
```

#### 5. 删除 Application
```bash
# 删除默认集群的 Application
DELETE /api/kubernetes/applications/my-app

# 删除指定集群的 Application
DELETE /api/kubernetes/applications/my-app?cluster=production-cluster

# 删除指定集群指定命名空间的 Application
DELETE /api/kubernetes/applications/my-app?namespace=default&cluster=staging-cluster
```

### Microservice 资源管理

#### 1. 获取 Microservice 列表
```bash
# 获取默认集群的所有 Microservice
GET /api/kubernetes/microservices

# 获取指定集群的所有 Microservice
GET /api/kubernetes/microservices?cluster=production-cluster

# 获取指定集群指定命名空间的 Microservice
GET /api/kubernetes/microservices?namespace=default&cluster=staging-cluster
```

#### 2. 获取 Microservice 详情
```bash
# 获取默认集群的 Microservice
GET /api/kubernetes/microservices/my-service

# 获取指定集群的 Microservice
GET /api/kubernetes/microservices/my-service?cluster=production-cluster

# 获取指定集群指定命名空间的 Microservice
GET /api/kubernetes/microservices/my-service?namespace=default&cluster=staging-cluster
```

#### 3. 创建 Microservice
```bash
# 在默认集群创建 Microservice
POST /api/kubernetes/microservices
Content-Type: application/json

# 在指定集群创建 Microservice
POST /api/kubernetes/microservices?cluster=production-cluster
Content-Type: application/json

# 在指定集群指定命名空间创建 Microservice
POST /api/kubernetes/microservices?namespace=default&cluster=staging-cluster
Content-Type: application/json
```

#### 4. 更新 Microservice
```bash
# 更新默认集群的 Microservice
PUT /api/kubernetes/microservices/my-service
Content-Type: application/json

# 更新指定集群的 Microservice
PUT /api/kubernetes/microservices/my-service?cluster=production-cluster
Content-Type: application/json

# 更新指定集群指定命名空间的 Microservice
PUT /api/kubernetes/microservices/my-service?namespace=default&cluster=staging-cluster
Content-Type: application/json
```

#### 5. 删除 Microservice
```bash
# 删除默认集群的 Microservice
DELETE /api/kubernetes/microservices/my-service

# 删除指定集群的 Microservice
DELETE /api/kubernetes/microservices/my-service?cluster=production-cluster

# 删除指定集群指定命名空间的 Microservice
DELETE /api/kubernetes/microservices/my-service?namespace=default&cluster=staging-cluster
```

## 响应格式

所有 API 响应都包含集群信息：

```json
{
  "cluster": "production-cluster",
  "namespace": "default",
  "applications": [...],
  "count": 5
}
```

错误响应也包含集群信息：

```json
{
  "error": "Failed to get applications",
  "cluster": "production-cluster",
  "message": "Connection timeout"
}
```

## 集群配置

### 自动集群初始化

系统会自动为每个新的集群名初始化连接：

1. 首先尝试使用标准 kubeconfig (`ClientBuilder.standard()`)
2. 如果失败，尝试集群内连接 (`ClientBuilder.cluster()`)
3. 如果都失败，使用默认 ApiClient

### 集群连接管理

- 使用 `ConcurrentHashMap` 管理多个集群的连接
- 按需初始化集群连接
- 支持动态添加新集群

## 使用示例

### 1. 检查多个集群的连接状态

```bash
# 检查默认集群
curl http://localhost:8080/api/kubernetes/connection

# 检查生产集群
curl http://localhost:8080/api/kubernetes/connection?cluster=production

# 检查测试集群
curl http://localhost:8080/api/kubernetes/connection?cluster=staging
```

### 2. 跨集群部署应用

```bash
# 在生产集群创建应用
curl -X POST http://localhost:8080/api/kubernetes/applications?cluster=production&namespace=prod \
  -H "Content-Type: application/json" \
  -d @application.json

# 在测试集群创建相同应用
curl -X POST http://localhost:8080/api/kubernetes/applications?cluster=staging&namespace=test \
  -H "Content-Type: application/json" \
  -d @application.json
```

### 3. 比较不同集群的资源

```bash
# 获取生产集群的应用列表
curl http://localhost:8080/api/kubernetes/applications?cluster=production

# 获取测试集群的应用列表
curl http://localhost:8080/api/kubernetes/applications?cluster=staging
```

## 注意事项

1. **集群名**: 集群名是区分大小写的
2. **连接管理**: 每个集群的连接是独立的
3. **错误处理**: 如果指定集群连接失败，会返回相应的错误信息
4. **性能**: 首次访问新集群时会进行连接初始化，可能需要一些时间
5. **安全性**: 确保不同集群的 kubeconfig 配置正确且有适当的权限

## 版本信息

- **版本**: 2.1.0
- **多集群支持**: ✅
- **类型安全**: ✅
- **默认集群**: cluster-local 