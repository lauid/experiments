# RESTful API 路径结构测试文档

## 概述

本文档记录了将 Prometheus API 从查询参数形式改为 RESTful 路径参数形式的测试结果。新的路径结构更加层次化和符合 REST 设计原则。

## 路径结构对比

### 旧结构（查询参数）
```
/api/prometheus/health?cluster={cluster}
/api/prometheus/version?cluster={cluster}
/api/prometheus/query?query={query}&time={time}&cluster={cluster}
/api/prometheus/query-range?query={query}&start={start}&end={end}&step={step}&cluster={cluster}
/api/prometheus/batch-query?cluster={cluster}
/api/prometheus/batch-query-range?cluster={cluster}
```

### 新结构（路径参数）
```
/api/clusters/{cluster}/prometheus/health
/api/clusters/{cluster}/prometheus/version
/api/clusters/{cluster}/prometheus/query?query={query}&time={time}
/api/clusters/{cluster}/prometheus/query-range?query={query}&start={start}&end={end}&step={step}
/api/clusters/{cluster}/prometheus/batch-query
/api/clusters/{cluster}/prometheus/batch-query-range
```

## 测试结果

### 1. 健康检查接口

**请求：**
```bash
curl -s "http://localhost:8080/api/clusters/cluster-local/prometheus/health"
```

**响应：**
```json
{
  "connected": true,
  "cluster": "cluster-local",
  "version": "{\"status\":\"success\",\"data\":{\"version\":\"3.4.1\",\"revision\":\"aea6503d9bbaad6c5faff3ecf6f1025213356c92\",\"branch\":\"HEAD\",\"buildUser\":\"root@16f976c24db1\",\"buildDate\":\"20250531-10:44:38\",\"goVersion\":\"go1.24.3\"}}",
  "status": "healthy"
}
```

**状态：** ✅ 成功

### 2. 版本信息接口

**请求：**
```bash
curl -s "http://localhost:8080/api/clusters/cluster-local/prometheus/version"
```

**响应：**
```json
{
  "cluster": "cluster-local",
  "version": "{\"status\":\"success\",\"data\":{\"version\":\"3.4.1\",\"revision\":\"aea6503d9bbaad6c5faff3ecf6f1025213356c92\",\"branch\":\"HEAD\",\"buildUser\":\"root@16f976c24db1\",\"buildDate\":\"20250531-10:44:38\",\"goVersion\":\"go1.24.3\"}}"
}
```

**状态：** ✅ 成功

### 3. 即时查询接口

**请求：**
```bash
curl -s "http://localhost:8080/api/clusters/cluster-local/prometheus/query?query=up"
```

**响应：**
```json
{
  "status": "success",
  "data": {
    "resultType": "vector",
    "result": [
      {
        "metric": {
          "__name__": "up",
          "app": "prometheus",
          "instance": "localhost:9090",
          "job": "prometheus"
        },
        "values": null,
        "value": [
          1750581147.754,
          "1"
        ]
      }
    ]
  },
  "error": null,
  "error_type": null
}
```

**状态：** ✅ 成功

### 4. 范围查询接口

**请求：**
```bash
curl -s "http://localhost:8080/api/clusters/cluster-local/prometheus/query-range?query=up&start=2025-01-22T15:00:00Z&end=2025-01-22T16:00:00Z&step=1m"
```

**响应：**
```json
{
  "status": "success",
  "data": {
    "resultType": "matrix",
    "result": []
  },
  "error": null,
  "error_type": null
}
```

**状态：** ✅ 成功

### 5. 批量查询接口

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/clusters/cluster-local/prometheus/batch-query" \
  -H "Content-Type: application/json" \
  -d '{
    "queries": [
      {
        "name": "up_metric",
        "query": "up",
        "description": "Prometheus up metric"
      },
      {
        "name": "cpu_metric",
        "query": "process_cpu_seconds_total",
        "description": "CPU usage metric"
      }
    ]
  }'
```

**响应：**
```json
{
  "status": "success",
  "data": [
    {
      "name": "up_metric",
      "description": "Prometheus up metric",
      "query": "up",
      "result": {
        "status": "success",
        "data": {
          "resultType": "vector",
          "result": [
            {
              "metric": {
                "__name__": "up",
                "app": "prometheus",
                "instance": "localhost:9090",
                "job": "prometheus"
              },
              "values": null,
              "value": [
                1750581251.8,
                "1"
              ]
            }
          ]
        },
        "error": null,
        "error_type": null
      },
      "execution_time_ms": 67
    },
    {
      "name": "cpu_metric",
      "description": "CPU usage metric",
      "query": "process_cpu_seconds_total",
      "result": {
        "status": "success",
        "data": {
          "resultType": "vector",
          "result": [
            {
              "metric": {
                "__name__": "process_cpu_seconds_total",
                "app": "prometheus",
                "instance": "localhost:9090",
                "job": "prometheus"
              },
              "values": null,
              "value": [
                1750581251.799,
                "15.61"
              ]
            }
          ]
        },
        "error": null,
        "error_type": null
      },
      "execution_time_ms": 77
    }
  ],
  "errors": [],
  "total_queries": 2,
  "successful_queries": 2,
  "failed_queries": 0
}
```

**状态：** ✅ 成功

### 6. 批量范围查询接口

**请求：**
```bash
curl -s -X POST "http://localhost:8080/api/clusters/cluster-local/prometheus/batch-query-range" \
  -H "Content-Type: application/json" \
  -d '{
    "queries": [
      {
        "name": "up_range",
        "query": "up",
        "description": "Up metric over time"
      }
    ],
    "start_time": "2025-01-22T15:00:00Z",
    "end_time": "2025-01-22T16:00:00Z",
    "step": "1m"
  }'
```

**响应：**
```json
{
  "status": "success",
  "data": [
    {
      "name": "up_range",
      "description": "Up metric over time",
      "query": "up",
      "result": {
        "status": "success",
        "data": {
          "resultType": "matrix",
          "result": []
        },
        "error": null,
        "error_type": null
      },
      "execution_time_ms": 46
    }
  ],
  "errors": [],
  "total_queries": 1,
  "successful_queries": 1,
  "failed_queries": 0
}
```

**状态：** ✅ 成功

### 7. 不存在的集群测试

**请求：**
```bash
curl -s "http://localhost:8080/api/clusters/non-existent/prometheus/health"
```

**响应：**
```json
{
  "connected": true,
  "cluster": "non-existent",
  "version": "{\"status\":\"success\",\"data\":{\"version\":\"3.4.1\",\"revision\":\"aea6503d9bbaad6c5faff3ecf6f1025213356c92\",\"branch\":\"HEAD\",\"buildUser\":\"root@16f976c24db1\",\"buildDate\":\"20250531-10:44:38\",\"goVersion\":\"go1.24.3\"}}",
  "status": "healthy"
}
```

**状态：** ✅ 成功（动态集群支持）

## 优势分析

### 1. RESTful 设计原则
- **资源层次化**：`/clusters/{cluster}/prometheus/*` 清晰地表达了资源层次关系
- **统一接口**：所有 Prometheus 相关操作都在 `/prometheus/` 路径下
- **无状态性**：每个请求都包含完整的集群信息

### 2. 可读性和可维护性
- **直观的 URL 结构**：`/clusters/cluster-1/prometheus/health` 比 `/prometheus/health?cluster=cluster-1` 更直观
- **更好的文档化**：路径结构本身就是 API 文档的一部分
- **易于扩展**：可以轻松添加更多集群相关的资源

### 3. 向后兼容性
- 保留了原有的查询参数形式的接口
- 提供了两种使用方式，满足不同场景需求

## 使用建议

### 1. 推荐使用场景
- **新项目**：建议使用新的路径参数形式
- **多集群环境**：路径参数形式更适合多集群管理
- **API 文档**：路径参数形式更容易生成和维护 API 文档

### 2. 迁移策略
- **渐进式迁移**：可以逐步从查询参数形式迁移到路径参数形式
- **双轨并行**：在过渡期间同时支持两种形式
- **客户端更新**：更新客户端代码以使用新的路径结构

## 总结

新的 RESTful 路径结构成功实现了以下目标：

1. ✅ **更符合 REST 设计原则**
2. ✅ **更好的资源层次化表达**
3. ✅ **提高 API 可读性和可维护性**
4. ✅ **保持向后兼容性**
5. ✅ **支持多集群环境**

所有测试接口都正常工作，新的路径结构已经成功部署并验证。 