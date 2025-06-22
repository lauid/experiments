# 新增自定义资源（CRD）完整流程示例（以 GPU 为例）

本流程以 GPU 资源为例，介绍如何在本项目中新增一个自定义资源（CRD），并实现端到端的 API 支持。

---

## 1. 定义 CRD YAML

编写 `gpu-crd.yaml`，内容如下：

```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: gpus.example.com
spec:
  group: example.com
  names:
    kind: GPU
    listKind: GPUList
    plural: gpus
    singular: gpu
    shortNames:
    - gpu
  scope: Namespaced
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              model:
                type: string
              ... # 省略其余字段，详见 gpu-crd.yaml
```

> **注意**：如需支持复杂结构，建议用 `type: object` + `properties`，避免使用 Kubernetes 1.25+ 禁止的 `dependencies` 字段。

---

## 2. 应用 CRD 到集群

```sh
kubectl apply -f gpu-crd.yaml
```

可用 `kubectl get crd` 验证。

---

## 3. 新增 Java 模型类

在 `src/main/java/com/example/kdemo/model/` 下新增：
- `GPU.java`      —— 资源主类，实现 `KubernetesObject`
- `GPUSpec.java`  —— 资源规格类，对应 CRD 的 `spec`
- `GPUStatus.java`—— 资源状态类，对应 CRD 的 `status`
- `GPUList.java`  —— 资源列表类，实现 `KubernetesListObject`

字段与 CRD 保持一致，支持类型安全。

---

## 4. 后端接口层支持

### 4.1 Repository
- 在 `KubernetesRepository` 接口中添加 GPU 相关方法声明
- 在 `KubernetesRepositoryImpl` 实现类中：
  - 初始化 `GenericKubernetesApi<GPU, GPUList>`
  - 实现 GPU 的 CRUD 方法

### 4.2 Service
- 在 `KubernetesService` 中添加 GPU 相关的业务方法

### 4.3 Controller
- 在 `KubernetesController` 中添加 GPU 相关的 REST API 端点

### 4.4 首页文档
- 在 `HomeController` 中补充 GPU 相关 API 文档说明

---

## 5. 示例资源 YAML/JSON

### YAML 示例
见 `example-gpu.yaml`：
```yaml
apiVersion: example.com/v1
kind: GPU
metadata:
  name: rtx-4090-01
  namespace: default
spec:
  model: "RTX 4090"
  ...
```

### JSON 示例
见 `example-gpu.json`。

---

## 6. 通过 REST API 操作 GPU 资源

- 列表：`GET /api/kubernetes/gpus?namespace=default`
- 查询：`GET /api/kubernetes/gpus/{name}?namespace=default`
- 创建：`POST /api/kubernetes/gpus?namespace=default`（body 支持 JSON/YAML）
- 更新：`PUT /api/kubernetes/gpus/{name}?namespace=default`
- 删除：`DELETE /api/kubernetes/gpus/{name}?namespace=default`

> **建议**：用 Postman/curl 测试，body 推荐用 JSON 格式。

---

## 7. 常见问题与排查

- **CRD 应用失败**：检查 YAML 是否符合 Kubernetes 1.25+ 规范，避免 `dependencies` 字段。
- **API 404**：确认 Controller 层已注册对应端点，服务已重启。
- **JSON 解析失败**：请将 YAML 转为 JSON 后再 POST。
- **多集群支持**：所有接口均支持 `cluster` 参数，未指定时默认 `cluster-local`。

---

## 8. 扩展说明

- 新增其他 CRD 资源时，流程与 GPU 完全一致。
- 推荐先用 YAML 测试 CRD 和资源，再开发后端类型安全模型。
- 如需前端页面支持，可直接复用 REST API。

---

如需更详细的代码示例或遇到特殊问题，请参考项目内现有 GPU 相关实现或联系维护者。 