# Kubernetes Secret TLS证书使用指南

## 概述

kdemo项目支持从Kubernetes Secret中获取TLS证书，用于安全连接Prometheus API。

## 配置

### application.properties
```properties
# Prometheus集群地址
prometheus.clusters.cluster-local=http://localhost:9090
prometheus.clusters.cluster-1=https://10.1.2.3:9090

# 从Secret获取TLS证书
prometheus.secret-tls-configs.cluster-1.namespace=monitoring
prometheus.secret-tls-configs.cluster-1.secret-name=prometheus-tls-certs
prometheus.secret-tls-configs.cluster-1.skip-ssl-verification=false
```

### Kubernetes Secret结构
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: prometheus-tls-certs
  namespace: monitoring
type: Opaque
data:
  ca.crt: <base64-ca-cert>
  tls.crt: <base64-client-cert>
  tls.key: <base64-client-key>
```

## 工作流程

1. cluster-local: 使用HTTP，无需TLS
2. 其他集群: 从Secret获取证书，使用HTTPS
3. 证书获取失败: 降级到默认连接

## 支持的证书密钥名称

- CA证书: `ca.crt`, `ca-cert.pem`, `ca.pem`
- 客户端证书: `tls.crt`, `client-cert.pem`, `cert.pem`
- 客户端私钥: `tls.key`, `client-key.pem`, `key.pem`

## 功能特性

- **自动证书管理**: 从Kubernetes Secret自动获取TLS证书
- **多集群支持**: 为不同集群配置不同的证书
- **缓存机制**: 缓存Secret数据，避免频繁查询
- **容错处理**: TLS配置失败时自动降级到默认连接
- **灵活配置**: 支持跳过SSL验证等选项

## 使用场景

### 1. cluster-local集群
- 不需要TLS配置
- 使用HTTP连接
- 适用于本地开发环境

### 2. 远程集群（cluster-1, cluster-2等）
- 从Kubernetes Secret获取TLS证书
- 使用HTTPS安全连接
- 支持双向TLS认证

### 3. 跳过SSL验证（开发环境）
```properties
prometheus.secret-tls-configs.cluster-dev.namespace=monitoring
prometheus.secret-tls-configs.cluster-dev.secret-name=prometheus-dev-certs
prometheus.secret-tls-configs.cluster-dev.skip-ssl-verification=true
```

## 工作流程

1. **配置检查**: 系统检查是否为cluster-local集群
2. **Secret配置**: 获取集群的Secret TLS配置
3. **证书获取**: 从Kubernetes API获取Secret中的证书数据
4. **SSL上下文**: 使用证书创建SSL上下文
5. **WebClient创建**: 创建支持TLS的WebClient
6. **缓存管理**: 缓存WebClient实例以提高性能

## 错误处理

### 1. Secret不存在
- 记录警告日志
- 使用默认WebClient（无TLS）

### 2. 证书格式错误
- 记录错误日志
- 降级到默认连接

### 3. SSL配置失败
- 记录详细错误信息
- 使用默认WebClient

## 监控和日志

### 日志级别
- `DEBUG`: 详细的TLS配置过程
- `INFO`: 成功获取证书和创建WebClient
- `WARN`: Secret不存在或证书无效
- `ERROR`: SSL配置失败

### 关键日志示例
```
DEBUG - Fetching secret prometheus-tls-certs from namespace monitoring for cluster cluster-1
DEBUG - Successfully fetched secret data with 3 keys
DEBUG - TLS config built successfully with CA: true, Client Cert: true, Client Key: true
DEBUG - Creating WebClient with TLS configuration for cluster cluster-1
INFO - Prometheus connection check successful for cluster: cluster-1
```

## 最佳实践

### 1. Secret管理
- 使用适当的命名空间隔离
- 定期轮换证书
- 设置适当的RBAC权限

### 2. 配置管理
- 为不同环境使用不同的Secret
- 避免在生产环境跳过SSL验证
- 使用有意义的Secret名称

### 3. 监控
- 监控证书过期时间
- 监控TLS连接成功率
- 设置证书更新告警

## 故障排除

### 1. 连接失败
- 检查Secret是否存在
- 验证证书格式是否正确
- 确认集群地址是否可访问

### 2. 证书错误
- 检查证书是否过期
- 验证证书链是否完整
- 确认私钥格式是否正确

### 3. 权限问题
- 检查ServiceAccount权限
- 验证Secret访问权限
- 确认命名空间权限

## 示例配置

### 完整的application.properties示例
```properties
# Prometheus配置
prometheus.clusters.cluster-local=http://localhost:9090
prometheus.clusters.cluster-prod=https://prometheus-prod.example.com:9090
prometheus.clusters.cluster-staging=https://prometheus-staging.example.com:9090

# 超时和并发配置
prometheus.timeout=30000
prometheus.max-concurrency=10

# TLS配置
prometheus.secret-tls-configs.cluster-prod.namespace=monitoring
prometheus.secret-tls-configs.cluster-prod.secret-name=prometheus-prod-certs
prometheus.secret-tls-configs.cluster-prod.skip-ssl-verification=false

prometheus.secret-tls-configs.cluster-staging.namespace=monitoring
prometheus.secret-tls-configs.cluster-staging.secret-name=prometheus-staging-certs
prometheus.secret-tls-configs.cluster-staging.skip-ssl-verification=false
```

### 对应的Kubernetes Secret示例
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: prometheus-prod-certs
  namespace: monitoring
type: Opaque
data:
  ca.crt: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCg==
  tls.crt: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCg==
  tls.key: LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQo=
```

这个配置确保了kdemo应用能够安全地连接到多个Prometheus集群，同时保持了配置的灵活性和可维护性。 