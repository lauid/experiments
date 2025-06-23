package com.example.kdemo.dto;

public class ResourceQuery {
    private String namespace;
    private String fieldSelector;
    private String labelSelector;
    private Integer limit;
    private String continueToken;

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public String getFieldSelector() { return fieldSelector; }
    public void setFieldSelector(String fieldSelector) { this.fieldSelector = fieldSelector; }
    public String getLabelSelector() { return labelSelector; }
    public void setLabelSelector(String labelSelector) { this.labelSelector = labelSelector; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public String getContinueToken() { return continueToken; }
    public void setContinueToken(String continueToken) { this.continueToken = continueToken; }
} 