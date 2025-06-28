#!/usr/bin/env python3
import json
import requests

# 获取所有指标名称
def get_metrics():
    response = requests.get("http://localhost:19090/api/v1/label/__name__/values")
    if response.status_code == 200:
        data = response.json()
        return data.get('data', [])
    return []

# 生成批量查询请求
def generate_batch_query(metrics, count=1000):
    # 如果指标数量不足，重复使用现有指标
    if len(metrics) < count:
        repeated_metrics = []
        while len(repeated_metrics) < count:
            repeated_metrics.extend(metrics)
        metrics = repeated_metrics[:count]
    
    queries = []
    for i, metric in enumerate(metrics[:count]):
        queries.append({
            "name": f"metric_{i}",
            "description": f"Query for {metric}",
            "query": metric
        })
    
    return {
        "queries": queries
    }

if __name__ == "__main__":
    print("获取 Prometheus 指标列表...")
    metrics = get_metrics()
    print(f"获取到 {len(metrics)} 个指标")
    
    print("生成 1000 个指标的批量查询请求...")
    batch_query = generate_batch_query(metrics, 1000)
    
    # 保存到文件
    with open("batch_query_1000.json", "w") as f:
        json.dump(batch_query, f, indent=2)
    
    print("批量查询请求已保存到 batch_query_1000.json")
    print(f"包含 {len(batch_query['queries'])} 个查询")
    
    # 显示前几个指标作为示例
    print("\n前 10 个指标:")
    for i, query in enumerate(batch_query['queries'][:10]):
        print(f"{i+1}. {query['query']}") 