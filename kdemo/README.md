# K8s Demo Application

这是一个集成了 Kubernetes 官方 OpenAPI 客户端的 Spring Boot 应用程序。

## 功能特性

- 使用 Kubernetes 官方 Java 客户端库 (`io.kubernetes:client-java`)
- 提供 REST API 来查询 Kubernetes 集群信息
- 支持获取命名空间、Pod 等资源信息
- 自动检测 Kubernetes 连接配置

## 依赖

- Spring Boot 3.5.3
- Kubernetes Client Java 24.0.0
- Java 17

## API 端点

### 首页
- `GET /` - 应用程序欢迎页面和 API 信息

### Kubernetes API
- `GET /api/kubernetes/status` - 检查 Kubernetes 连接状态
- `GET /api/kubernetes/namespaces` - 获取所有命名空间
- `GET /api/kubernetes/namespaces/{namespace}/pods` - 获取指定命名空间中的所有 Pod
- `GET /api/kubernetes/overview` - 获取集群概览信息

## 运行应用程序

### 使用 Maven Wrapper
```bash
./mvnw spring-boot:run
```

### 使用 Maven
```bash
mvn spring-boot:run
```

## Kubernetes 配置

应用程序会自动尝试以下配置方式：

1. **本地开发环境**: 从 `~/.kube/config` 文件加载配置
2. **集群内运行**: 使用集群内服务账户配置

### 本地开发

确保您已经配置了 kubectl 并且可以访问 Kubernetes 集群：

```bash
# 检查 kubectl 配置
kubectl config current-context

# 测试连接
kubectl get namespaces
```

### 集群内运行

如果应用程序在 Kubernetes 集群内运行，确保：

1. 创建了适当的 ServiceAccount
2. 配置了必要的 RBAC 权限
3. 挂载了服务账户令牌

示例 ServiceAccount 配置：

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: kdemo-service-account
  namespace: default
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: kdemo-reader
rules:
- apiGroups: [""]
  resources: ["namespaces", "pods"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: kdemo-reader-binding
subjects:
- kind: ServiceAccount
  name: kdemo-service-account
  namespace: default
roleRef:
  kind: ClusterRole
  name: kdemo-reader
  apiGroup: rbac.authorization.k8s.io
```

## 测试 API

启动应用程序后，您可以使用以下命令测试 API：

```bash
# 检查应用程序状态
curl http://localhost:8080/

# 检查 Kubernetes 连接状态
curl http://localhost:8080/api/kubernetes/status

# 获取所有命名空间
curl http://localhost:8080/api/kubernetes/namespaces

# 获取默认命名空间中的 Pod
curl http://localhost:8080/api/kubernetes/namespaces/default/pods

# 获取集群概览
curl http://localhost:8080/api/kubernetes/overview
```

## 示例响应

### 集群概览响应
```json
{
  "connected": true,
  "namespaces": 5,
  "totalPods": 12,
  "namespaceList": ["default", "kube-system", "kube-public", "kube-node-lease", "ingress-nginx"]
}
```

### 命名空间列表响应
```json
{
  "namespaces": ["default", "kube-system", "kube-public", "kube-node-lease", "ingress-nginx"],
  "count": 5
}
```

## 故障排除

### 连接问题

如果遇到连接问题，请检查：

1. kubectl 配置是否正确
2. Kubernetes 集群是否可访问
3. 网络连接是否正常
4. 防火墙设置

### 权限问题

如果遇到权限问题，请检查：

1. 用户是否有足够的权限
2. RBAC 配置是否正确
3. ServiceAccount 是否配置正确

## 开发

### 添加新的 Kubernetes 资源

要添加对其他 Kubernetes 资源的支持，可以参考现有的 `KubernetesService` 类，添加相应的方法和 API 端点。

### 扩展功能

- 添加对 Deployment、Service 等资源的支持
- 实现资源创建、更新、删除功能
- 添加事件监听功能
- 实现资源监控和告警 