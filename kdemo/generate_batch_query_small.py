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

# 生成小规模批量查询请求
def generate_small_batch_query(metrics, count=50):
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
    
    print("生成 50 个指标的批量查询请求...")
    batch_query = generate_small_batch_query(metrics, 50)
    
    # 保存到文件
    with open("batch_query_50.json", "w") as f:
        json.dump(batch_query, f, indent=2)
    
    print("批量查询请求已保存到 batch_query_50.json")
    print(f"包含 {len(batch_query['queries'])} 个查询") 