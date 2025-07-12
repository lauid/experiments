package com.example.kdemo.service;

import com.example.kdemo.dto.PrometheusBatchQueryResponse;
import com.example.kdemo.dto.PrometheusBatchRangeQueryRequest;
import com.example.kdemo.dto.PrometheusQueryRequest;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import java.util.List;

/**
 * Prometheus服务接口
 */
public interface PrometheusService {
    
    /**
     * 批量查询多个指标
     * 
     * @param cluster 集群名
     * @param request 批量查询请求
     * @return 批量查询结果
     * @throws PrometheusException 查询异常
     */
    PrometheusBatchQueryResponse batchQuery(String cluster, PrometheusQueryRequest request) throws PrometheusException;
    
    /**
     * 批量范围查询多个指标
     * 
     * @param cluster 集群名
     * @param request 批量范围查询请求
     * @return 批量范围查询结果
     * @throws PrometheusException 查询异常
     */
    PrometheusBatchQueryResponse batchQueryRange(String cluster, PrometheusBatchRangeQueryRequest request) throws PrometheusException;
    
    /**
     * 执行单个范围查询
     * 
     * @param cluster 集群名
     * @param query PromQL查询语句
     * @param startTime 开始时间（时间戳，毫秒）
     * @param endTime 结束时间（时间戳，毫秒）
     * @param step 步长
     * @return 查询结果
     * @throws PrometheusException 查询异常
     */
    PrometheusQueryResponse queryRange(String cluster, String query, Long startTime, Long endTime, String step) 
            throws PrometheusException;
    
    /**
     * 执行单个即时查询
     * 
     * @param cluster 集群名
     * @param query PromQL查询语句
     * @param time 查询时间（时间戳，毫秒，可选）
     * @return 查询结果
     * @throws PrometheusException 查询异常
     */
    PrometheusQueryResponse query(String cluster, String query, Long time) throws PrometheusException;
    
    /**
     * 检查Prometheus连接状态
     * 
     * @param cluster 集群名
     * @return 连接状态
     * @throws PrometheusException 连接异常
     */
    Boolean checkConnection(String cluster) throws PrometheusException;
    
    /**
     * 获取Prometheus版本信息
     * 
     * @param cluster 集群名
     * @return 版本信息
     * @throws PrometheusException 查询异常
     */
    String getVersion(String cluster) throws PrometheusException;
    
    /**
     * 获取预定义的指标查询模板
     * 
     * @return 指标查询模板列表
     */
    List<PrometheusQueryRequest.MetricQuery> getMetricTemplates();
} 