# Kubernetes CRD 类型安全 API 使用指南

这个Spring Boot应用程序提供了类型安全的Kubernetes CRD（Custom Resource Definition）管理功能，使用 `GenericKubernetesApi` 和强类型Java类，避免了动态对象和反射的使用。

## 架构特点

- **类型安全**: 使用强类型Java类 `Application`, `ApplicationSpec`, `ApplicationStatus`
- **无反射**: 直接使用 `GenericKubernetesApi<Application, ApplicationList>`
- **编译时检查**: 所有API调用在编译时进行类型检查
- **生产就绪**: 适合生产环境使用

## API 端点

### CRD 管理

#### 1. 获取所有CRD
```bash
GET /api/kubernetes/crds
```

#### 2. 获取指定CRD的详细信息
```bash
GET /api/kubernetes/crds/{name}
```

#### 3. 创建CRD
```bash
POST /api/kubernetes/crds
Content-Type: application/json

# 请求体是CRD的YAML内容（JSON格式）
```

### 类型安全的 Application 资源 CRUD 操作

#### 1. 获取所有 Application 资源（列表）
```bash
GET /api/kubernetes/applications?namespace={namespace}
```

#### 2. 获取指定的 Application 资源（单个）
```bash
GET /api/kubernetes/applications/{name}?namespace={namespace}
```

#### 3. 创建 Application 资源
```bash
POST /api/kubernetes/applications?namespace={namespace}
Content-Type: application/json

# 请求体是 Application 资源的 JSON 格式
```

#### 4. 更新 Application 资源
```bash
PUT /api/kubernetes/applications/{name}?namespace={namespace}
Content-Type: application/json

# 请求体是更新后的 Application 资源 JSON 格式
```

#### 5. 删除 Application 资源
```bash
DELETE /api/kubernetes/applications/{name}?namespace={namespace}
```

## 使用示例

### 1. 创建CRD示例

1. 首先启动应用程序：
```bash
./mvnw spring-boot:run
```

2. 创建CRD（使用curl）：
```bash
# 将 YAML 转换为 JSON
yq -o=json example-crd.yaml > example-crd.json

curl -X POST http://localhost:8080/api/kubernetes/crds \
  -H "Content-Type: application/json" \
  -d @example-crd.json
```

3. 查看创建的CRD：
```bash
curl http://localhost:8080/api/kubernetes/crds/applications.example.com
```

### 2. Application 资源完整CRUD示例

#### 创建 Application 资源
```bash
# 将 YAML 转换为 JSON
yq -o=json example-application.yaml > example-application.json

curl -X POST "http://localhost:8080/api/kubernetes/applications?namespace=default" \
  -H "Content-Type: application/json" \
  -d @example-application.json
```

#### 获取 Application 资源列表
```bash
curl "http://localhost:8080/api/kubernetes/applications?namespace=default"
```

#### 获取单个 Application 资源
```bash
curl "http://localhost:8080/api/kubernetes/applications/my-app?namespace=default"
```

#### 更新 Application 资源
```bash
# 将 YAML 转换为 JSON
yq -o=json example-application-updated.yaml > example-application-updated.json

curl -X PUT "http://localhost:8080/api/kubernetes/applications/my-app?namespace=default" \
  -H "Content-Type: application/json" \
  -d @example-application-updated.json
```

#### 删除 Application 资源
```bash
curl -X DELETE "http://localhost:8080/api/kubernetes/applications/my-app?namespace=default"
```

## 完整的API端点列表

| 操作 | HTTP方法 | 端点 | 说明 |
|------|----------|------|------|
| 获取CRD列表 | GET | `/api/kubernetes/crds` | 获取所有CRD |
| 获取CRD详情 | GET | `/api/kubernetes/crds/{name}` | 获取指定CRD的详细信息 |
| 创建CRD | POST | `/api/kubernetes/crds` | 创建新的CRD |
| 获取Application列表 | GET | `/api/kubernetes/applications` | 获取所有Application资源 |
| 获取单个Application | GET | `/api/kubernetes/applications/{name}` | 获取指定的Application资源 |
| 创建Application | POST | `/api/kubernetes/applications` | 创建新的Application资源 |
| 更新Application | PUT | `/api/kubernetes/applications/{name}` | 更新指定的Application资源 |
| 删除Application | DELETE | `/api/kubernetes/applications/{name}` | 删除指定的Application资源 |

## 类型安全的Java类结构

### Application.java
```java
public class Application implements KubernetesObject {
    private String apiVersion = "example.com/v1";
    private String kind = "Application";
    private V1ObjectMeta metadata;
    private ApplicationSpec spec;
    private ApplicationStatus status;
    // getters and setters
}
```

### ApplicationSpec.java
```java
public class ApplicationSpec {
    private String name;
    private String version;
    private Integer replicas;
    // getters and setters
}
```

### ApplicationStatus.java
```java
public class ApplicationStatus {
    private String phase;
    // getters and setters
}
```

## 示例文件

- `example-crd.yaml`: 示例CRD定义
- `example-application.yaml`: 示例Application资源
- `example-application-updated.yaml`: 更新后的Application资源

## 注意事项

1. **JSON格式**: 所有API请求体必须是JSON格式，不能直接使用YAML
2. **类型安全**: 使用强类型Java类，编译时进行类型检查
3. **命名空间**: Application资源是命名空间作用域的，需要指定namespace参数
4. **CRD依赖**: 创建Application资源前必须先创建对应的CRD
5. **错误处理**: 所有API都会返回适当的HTTP状态码和错误信息

## 错误处理

所有API都会返回适当的HTTP状态码和错误信息：
- 200: 成功
- 400: 请求参数错误（如JSON格式错误）
- 404: 资源不存在
- 500: 服务器内部错误

错误响应格式：
```json
{
  "error": "错误描述",
  "message": "详细错误信息",
  "success": false
}
```

成功响应格式：
```json
{
  "success": true,
  "name": "资源名称",
  "message": "操作成功信息"
}
```

## 优势

相比之前的动态API实现：

1. **类型安全**: 编译时检查，避免运行时类型错误
2. **性能更好**: 无反射调用，直接方法调用
3. **IDE支持**: 完整的代码补全和重构支持
4. **维护性**: 代码更清晰，易于维护和扩展
5. **生产就绪**: 适合生产环境使用

## 扩展其他CRD

如需支持其他CRD，只需：

1. 创建对应的Java类（如 `MyResource.java`, `MyResourceSpec.java` 等）
2. 实现 `KubernetesObject` 接口
3. 在 `KubernetesService` 中创建对应的 `GenericKubernetesApi` 实例
4. 添加对应的Controller方法

这种模式可以轻松扩展到任何自定义资源。

# CRD 使用说明（以 Microservice 为例）

## 1. 定义并应用复杂 CRD

建议直接用 `kubectl` 应用 YAML 文件：

```sh
kubectl apply -f complex-crd.yaml
```

如需删除：
```sh
kubectl delete -f complex-crd.yaml
```

## 2. 示例 Microservice 资源

复杂示例：
```yaml
apiVersion: example.com/v1
kind: Microservice
metadata:
  name: user-service
  namespace: default
spec:
  name: user-service
  image: myregistry.com/user-service:v1.0.0
  version: "1.0.0"
  replicas: 3
  resources:
    requests:
      cpu: "200m"
      memory: "256Mi"
    limits:
      cpu: "1000m"
      memory: "1Gi"
  ports:
    - name: http
      port: 8080
      targetPort: 8080
      protocol: TCP
  environment:
    - name: DATABASE_URL
      value: "postgresql://user-service-db:5432/users"
  networking:
    serviceType: ClusterIP
    ingress:
      enabled: true
      host: "user-service.example.com"
      path: "/api/users"
      tls: true
```

简化示例：
```yaml
apiVersion: example.com/v1
kind: Microservice
metadata:
  name: simple-service
  namespace: default
spec:
  name: simple-service
  image: nginx:latest
  version: "1.0.0"
  replicas: 2
```

## 3. YAML 转 JSON

如需通过 API 传递 JSON，可用如下命令转换：

```sh
python3 -c "import sys, yaml, json; print(json.dumps(yaml.safe_load(sys.stdin.read())))" < example-microservice.yaml > example-microservice.json
```

## 4. 通过 REST API 管理 CRD 及资源

### CRD 列表
```
GET /api/kubernetes/crds
```

### 获取 CRD 详情
```
GET /api/kubernetes/crds/{name}
```

### 创建 CRD（推荐用 kubectl，API 仅支持 JSON 格式）
```
POST /api/kubernetes/crds
Content-Type: application/json
Body: <CRD JSON>
```

### Microservice 资源管理

- 列表：
  - `GET /api/kubernetes/microservices?namespace=default`
- 查询单个：
  - `GET /api/kubernetes/microservices/{name}?namespace=default`
- 创建：
  - `POST /api/kubernetes/microservices?namespace=default`  
    Body: 资源 YAML 或 JSON
- 更新：
  - `PUT /api/kubernetes/microservices/{name}?namespace=default`  
    Body: 资源 YAML 或 JSON
- 删除：
  - `DELETE /api/kubernetes/microservices/{name}?namespace=default`

## 5. 常见报错说明

- **dependencies is not supported**：
  - 这是 Kubernetes 1.25+ 的 openAPIV3Schema 校验限制，YAML/JSON 里不要出现 `dependencies` 字段。
  - 建议直接用 YAML + kubectl 应用 CRD，避免用 Java/Jackson 转换后自动加字段。
- **items: Required value: must be specified**：
  - 定义数组类型时需加 `items` 字段，例如：
    ```json
    "ports": { "type": "array", "items": { "type": "object" } }
    ```
- **404 Not Found**：
  - 检查 Controller 路径、服务是否重启、代码是否编译。
- **500 Internal Server Error**：
  - 检查 YAML/JSON 格式、字段拼写、CRD 是否已注册。

## 6. 参考
- [Kubernetes CRD 官方文档](https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/custom-resource-definitions/)
- [client-java GenericKubernetesApi 用法](https://github.com/kubernetes-client/java/blob/master/examples/src/main/java/io/kubernetes/client/examples/GenericExample.java)

curl -X POST "http://localhost:8080/api/kubernetes/microservices?namespace=default" \
  -H "Content-Type: text/plain" \
  --data-binary @example-microservice.yaml 

curl -X GET "http://localhost:8080/api/kubernetes/microservices/user-service?namespace=default" 