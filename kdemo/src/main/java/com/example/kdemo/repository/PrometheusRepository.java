package com.example.kdemo.repository;

import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import reactor.core.publisher.Mono;

/**
 * Prometheus数据访问接口
 */
public interface PrometheusRepository {
    
    /**
     * 执行单个PromQL查询
     * 
     * @param cluster 集群名
     * @param query PromQL查询语句
     * @param startTime 开始时间（Unix时间戳或RFC3339格式）
     * @param endTime 结束时间（Unix时间戳或RFC3339格式）
     * @param step 步长（如：15s, 1m, 1h）
     * @return 查询结果
     * @throws PrometheusException 查询异常
     */
    Mono<PrometheusQueryResponse> queryRange(String cluster, String query, String startTime, String endTime, String step) 
            throws PrometheusException;
    
    /**
     * 执行即时查询
     * 
     * @param cluster 集群名
     * @param query PromQL查询语句
     * @param time 查询时间（Unix时间戳或RFC3339格式，可选）
     * @return 查询结果
     * @throws PrometheusException 查询异常
     */
    Mono<PrometheusQueryResponse> query(String cluster, String query, String time) throws PrometheusException;
    
    /**
     * 检查Prometheus连接状态
     * 
     * @param cluster 集群名
     * @return 连接状态
     * @throws PrometheusException 连接异常
     */
    Mono<Boolean> checkConnection(String cluster) throws PrometheusException;
    
    /**
     * 获取Prometheus版本信息
     * 
     * @param cluster 集群名
     * @return 版本信息
     * @throws PrometheusException 查询异常
     */
    Mono<String> getVersion(String cluster) throws PrometheusException;
} 