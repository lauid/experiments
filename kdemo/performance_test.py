#!/usr/bin/env python3
import json
import requests
import time
import statistics

def test_batch_query(queries_count, cluster="cluster-local"):
    """测试指定数量的批量查询"""
    print(f"\n测试 {queries_count} 个指标的批量查询...")
    
    # 生成测试数据
    metrics = [
        "up", "process_cpu_seconds_total", "process_resident_memory_bytes",
        "prometheus_http_requests_total", "prometheus_engine_queries",
        "go_goroutines", "go_threads", "go_memstats_alloc_bytes"
    ]
    
    # 重复指标以达到指定数量
    test_metrics = []
    while len(test_metrics) < queries_count:
        test_metrics.extend(metrics)
    test_metrics = test_metrics[:queries_count]
    
    # 构建查询请求
    queries = []
    for i, metric in enumerate(test_metrics):
        queries.append({
            "name": f"metric_{i}",
            "description": f"Query for {metric}",
            "query": metric
        })
    
    batch_request = {"queries": queries}
    
    # 执行测试
    start_time = time.time()
    response = requests.post(
        f"http://localhost:8080/api/prometheus/batch-query?cluster={cluster}",
        headers={"Content-Type": "application/json"},
        json=batch_request,
        timeout=300  # 5分钟超时
    )
    end_time = time.time()
    
    total_time = end_time - start_time
    
    if response.status_code == 200:
        result = response.json()
        successful = result.get('successful_queries', 0)
        failed = result.get('failed_queries', 0)
        total = result.get('total_queries', 0)
        
        # 计算执行时间统计
        execution_times = []
        if 'data' in result:
            execution_times = [item.get('execution_time_ms', 0) for item in result['data']]
        
        stats = {
            'total_time': total_time,
            'successful': successful,
            'failed': failed,
            'total': total,
            'avg_execution_time': statistics.mean(execution_times) if execution_times else 0,
            'min_execution_time': min(execution_times) if execution_times else 0,
            'max_execution_time': max(execution_times) if execution_times else 0,
            'throughput': successful / total_time if total_time > 0 else 0
        }
        
        print(f"  总时间: {total_time:.2f} 秒")
        print(f"  成功: {successful}, 失败: {failed}, 总计: {total}")
        print(f"  平均执行时间: {stats['avg_execution_time']:.2f} ms")
        print(f"  最小执行时间: {stats['min_execution_time']:.2f} ms")
        print(f"  最大执行时间: {stats['max_execution_time']:.2f} ms")
        print(f"  吞吐量: {stats['throughput']:.2f} 查询/秒")
        
        return stats
    else:
        print(f"  请求失败: {response.status_code}")
        print(f"  响应: {response.text}")
        return None

def main():
    print("Prometheus 批量查询性能测试")
    print("=" * 50)
    
    # 测试不同规模的批量查询
    test_sizes = [10, 50, 100, 200, 500, 1000]
    results = {}
    
    for size in test_sizes:
        result = test_batch_query(size)
        if result:
            results[size] = result
    
    # 输出总结
    print("\n" + "=" * 50)
    print("性能测试总结")
    print("=" * 50)
    print(f"{'查询数量':<10} {'总时间(秒)':<12} {'成功数':<8} {'失败数':<8} {'平均执行时间(ms)':<15} {'吞吐量(查询/秒)':<15}")
    print("-" * 80)
    
    for size, result in results.items():
        print(f"{size:<10} {result['total_time']:<12.2f} {result['successful']:<8} {result['failed']:<8} {result['avg_execution_time']:<15.2f} {result['throughput']:<15.2f}")

if __name__ == "__main__":
    main() 