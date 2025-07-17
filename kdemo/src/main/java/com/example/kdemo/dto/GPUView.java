package com.example.kdemo.dto;

import com.example.kdemo.model.GPU;
import com.example.kdemo.model.GPUSpec;

public class GPUView {
    // 你可以根据实际需求添加更多字段
    private String name;
    private String vendor;
    // ... 其他字段 ...

    public GPUView(GPU gpu) {
        this.name = gpu.getMetadata() != null ? gpu.getMetadata().getName() : null;
        GPUSpec spec = gpu.getSpec();
        this.vendor = (spec != null && spec.getVendor() != null) ? spec.getVendor().name() : null;
        // ... 其他字段赋值 ...
    }

    public String getName() { return name; }
    public String getVendor() { return vendor; }
    // ... 其他 getter/setter ...
} 