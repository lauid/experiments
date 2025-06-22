package com.example.kdemo.util;

public class ClusterUtils {
    
    public static final String DEFAULT_CLUSTER = "cluster-local";
    
    /**
     * 获取集群名称，如果为空则返回默认集群
     */
    public static String getClusterName(String cluster) {
        return cluster != null && !cluster.isEmpty() ? cluster : DEFAULT_CLUSTER;
    }
    
    /**
     * 验证集群名称是否有效
     */
    public static boolean isValidClusterName(String cluster) {
        return cluster != null && !cluster.trim().isEmpty();
    }
    
    /**
     * 验证命名空间名称是否有效
     */
    public static boolean isValidNamespace(String namespace) {
        return namespace != null && !namespace.trim().isEmpty();
    }
} 