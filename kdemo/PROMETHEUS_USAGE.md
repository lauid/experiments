# Prometheus批量查询接口使用指南

## 概述

本项目提供了完整的Prometheus批量查询接口，支持同时查询多个指标数据，具有良好的代码分层结构和错误处理机制。

## 代码分层结构

```
src/main/java/com/example/kdemo/
├── config/
│   └── PrometheusConfig.java          # Prometheus配置类
├── controller/
│   └── PrometheusController.java      # 控制器层
├── service/
│   ├── PrometheusService.java         # 服务接口
│   └── PrometheusServiceImpl.java     # 服务实现
├── repository/
│   ├── PrometheusRepository.java      # 数据访问接口
│   └── PrometheusRepositoryImpl.java  # 数据访问实现
├── dto/
│   ├── PrometheusQueryRequest.java    # 查询请求DTO
│   ├── PrometheusQueryResponse.java   # 查询响应DTO
│   └── PrometheusBatchQueryResponse.java # 批量查询响应DTO
└── exception/
    └── PrometheusException.java       # 自定义异常类
```

## API接口

### 1. 批量查询接口

**POST** `/api/prometheus/batch-query`

批量查询多个指标数据。

**请求体示例：**
```json
{
  "cluster": "cluster-1",
  "queries": [
    {
      "name": "cpu_usage",
      "query": "rate(container_cpu_usage_seconds_total{container!=\"\"}[5m]) * 100",
      "description": "Container CPU usage percentage",
      "labels": {
        "namespace": "default"
      }
    },
    {
      "name": "memory_usage",
      "query": "container_memory_usage_bytes{container!=\"\"} / container_spec_memory_limit_bytes{container!=\"\"} * 100",
      "description": "Container memory usage percentage",
      "labels": {
        "namespace": "default"
      }
    }
  ],
  "start_time": "2024-01-01T00:00:00Z",
  "end_time": "2024-01-01T01:00:00Z",
  "step": "1m"
}
```

**响应示例：**
```json
{
  "status": "success",
  "data": [
    {
      "name": "cpu_usage",
      "description": "Container CPU usage percentage",
      "query": "rate(container_cpu_usage_seconds_total{container!=\"\"}[5m]) * 100",
      "result": {
        "status": "success",
        "data": {
          "resultType": "matrix",
          "result": [
            {
              "metric": {
                "container": "app",
                "namespace": "default",
                "pod": "app-123"
              },
              "values": [
                [1640995200, "2.5"],
                [1640995260, "3.1"]
              ]
            }
          ]
        }
      },
      "execution_time_ms": 150
    }
  ],
  "errors": [],
  "total_queries": 2,
  "successful_queries": 2,
  "failed_queries": 0
}
```

### 2. 范围查询接口

**GET** `/api/prometheus/query-range`

执行单个范围查询。

**参数：**
- `query`: PromQL查询语句
- `start`: 开始时间（Unix时间戳或RFC3339格式）
- `end`: 结束时间（Unix时间戳或RFC3339格式）
- `step`: 步长（如：15s, 1m, 1h）

**示例：**
```
GET /api/prometheus/query-range?query=rate(container_cpu_usage_seconds_total[5m])&start=1640995200&end=1640998800&step=1m
```

### 3. 即时查询接口

**GET** `/api/prometheus/query`

执行单个即时查询。

**参数：**
- `query`: PromQL查询语句
- `time`: 查询时间（可选，Unix时间戳或RFC3339格式）

**示例：**
```
GET /api/prometheus/query?query=container_cpu_usage_seconds_total
```

### 4. 健康检查接口

**GET** `/api/prometheus/health`

检查Prometheus连接状态。

**响应示例：**
```json
{
  "status": "healthy",
  "connected": true,
  "version": "2.45.0"
}
```

### 5. 获取查询模板接口

**GET** `/api/prometheus/templates`

获取预定义的指标查询模板。

**响应示例：**
```json
{
  "templates": [
    {
      "name": "cpu_usage",
      "query": "rate(container_cpu_usage_seconds_total{container!=\"\"}[5m]) * 100",
      "description": "Container CPU usage percentage",
      "labels": {}
    }
  ],
  "count": 6
}
```

### 6. 版本信息接口

**GET** `/api/prometheus/version`

获取Prometheus版本信息。

**响应示例：**
```json
{
  "version": "2.45.0"
}
```

## 配置说明

在 `application.properties` 中配置Prometheus相关参数：

```properties
# Prometheus配置
prometheus.base-url=http://localhost:9090
prometheus.timeout=30000
prometheus.max-concurrency=10
prometheus.enable-retry=true
prometheus.max-retries=3
prometheus.retry-delay=1000
```

## 特性

### 1. 并发控制
- 支持配置最大并发查询数量
- 使用Reactor的flatMap实现并发查询
- 避免对Prometheus服务器造成过大压力

### 2. 错误处理
- 完善的异常处理机制
- 支持部分查询失败的情况
- 详细的错误信息返回

### 3. 性能优化
- 异步非阻塞查询
- 超时控制
- 重试机制

### 4. 可扩展性
- 支持自定义指标查询模板
- 灵活的配置管理
- 良好的代码分层结构

## 使用示例

### Java代码示例

```java
@Autowired
private PrometheusService prometheusService;

// 创建批量查询请求
PrometheusQueryRequest request = new PrometheusQueryRequest();
request.setCluster("cluster-1");

List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
queries.add(new PrometheusQueryRequest.MetricQuery(
    "cpu_usage",
    "rate(container_cpu_usage_seconds_total{container!=\"\"}[5m]) * 100",
    "Container CPU usage percentage",
    new HashMap<>()
));
request.setQueries(queries);

request.setStartTime("2024-01-01T00:00:00Z");
request.setEndTime("2024-01-01T01:00:00Z");
request.setStep("1m");

// 执行批量查询
prometheusService.batchQuery(request)
    .subscribe(response -> {
        System.out.println("查询成功，成功查询数: " + response.getSuccessfulQueries());
        response.getData().forEach(result -> {
            System.out.println("指标: " + result.getName() + ", 执行时间: " + result.getExecutionTimeMs() + "ms");
        });
    }, error -> {
        System.err.println("查询失败: " + error.getMessage());
    });
```

### cURL示例

```bash
# 批量查询
curl -X POST http://localhost:8080/api/prometheus/batch-query \
  -H "Content-Type: application/json" \
  -d '{
    "queries": [
      {
        "name": "cpu_usage",
        "query": "rate(container_cpu_usage_seconds_total[5m]) * 100",
        "description": "CPU usage"
      }
    ],
    "start_time": "2024-01-01T00:00:00Z",
    "end_time": "2024-01-01T01:00:00Z",
    "step": "1m"
  }'

# 健康检查
curl http://localhost:8080/api/prometheus/health

# 获取模板
curl http://localhost:8080/api/prometheus/templates
```

## 注意事项

1. 确保Prometheus服务器正在运行且可访问
2. 根据实际需求调整并发数量和超时时间
3. 合理使用查询模板，避免过于复杂的查询语句
4. 注意时间格式的正确性（支持Unix时间戳和RFC3339格式）
5. 监控查询性能，避免对Prometheus服务器造成过大压力 