package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public enum Vendor {
    HUAWEI("huawei"),
    NVIDIA("nvidia");

    private final String value;

    Vendor(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }


    @JsonCreator
    public static Vendor fromValue(String value) {
        for (Vendor v : Vendor.values()) {
            if (v.value.equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown vendor: " + value);
    }

    // Gson TypeAdapter for lowercase serialization/deserialization
    public static class Adapter extends TypeAdapter<Vendor> {
        @Override
        public void write(JsonWriter out, Vendor vendor) throws IOException {
            out.value(vendor == null ? null : vendor.value);
        }
        @Override
        public Vendor read(JsonReader in) throws IOException {
            String value = in.nextString();
            for (Vendor v : Vendor.values()) {
                if (v.value.equalsIgnoreCase(value)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unknown vendor: " + value);
        }
    }
} 