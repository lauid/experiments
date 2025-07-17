package com.example.kdemo.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GPUSpecTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testVendorEnumSerialization() throws Exception {
        GPUSpec spec = new GPUSpec();
        spec.setVendor(Vendor.NVIDIA);
        String json = objectMapper.writeValueAsString(spec);
        assertTrue(json.contains("\"vendor\":\"nvidia\""));
    }

    @Test
    void testVendorEnumDeserialization() throws Exception {
        String json = "{\"vendor\":\"huawei\"}";
        GPUSpec spec = objectMapper.readValue(json, GPUSpec.class);
        assertEquals(Vendor.HUAWEI, spec.getVendor());
    }

    @Test
    void testVendorGsonSerialization() {
        GPUSpec spec = new GPUSpec();
        spec.setVendor(Vendor.HUAWEI);
        Gson gson = new GsonBuilder().registerTypeAdapter(Vendor.class, new Vendor.Adapter()).create();
        String json = gson.toJson(spec);
        assertTrue(json.contains("\"vendor\":\"huawei\""));
    }

    @Test
    void testVendorGsonDeserialization() {
        String json = "{\"vendor\":\"nvidia\"}";
        Gson gson = new GsonBuilder().registerTypeAdapter(Vendor.class, new Vendor.Adapter()).create();
        GPUSpec spec = gson.fromJson(json, GPUSpec.class);
        assertEquals(Vendor.NVIDIA, spec.getVendor());
    }
} 