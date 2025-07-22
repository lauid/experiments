package com.example.kdemo.util;

import io.kubernetes.client.custom.Quantity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantityUtilsTest {

    @Test
    void testAddMemory() {
        String result = QuantityUtils.add("500Mi", "1Gi");
        assertEquals("1524Mi", result); // 500Mi+1Gi=1524Mi
    }

    @Test
    void testAddCpu() {
        String result = QuantityUtils.add("500m", "1");
        assertEquals("1500m", result); // 1=1000m+500m=1500m
    }

    @Test
    void testAddQuantitiesCpu() {
        Quantity q1 = new Quantity("250m");
        Quantity q2 = new Quantity("0.75");
        String result = QuantityUtils.addQuantities(q1, q2);
        assertEquals("1000m", result);
    }

    @Test
    void testAddDifferentMemoryUnits() {
        String result = QuantityUtils.add("1Gi", "1024Mi");
        assertEquals("2Gi", result);
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> QuantityUtils.add("abc", "1Gi"));
    }

    @Test
    void testAddMemoryUnits() {
        // Ki + Ki
        assertEquals("2Ki", QuantityUtils.add("1Ki", "1Ki"));
        // Mi + Ki
        assertEquals("1.0009765625Mi", QuantityUtils.add("1Mi", "1Ki"));
        // Gi + Mi
        assertEquals("1.0009765625Gi", QuantityUtils.add("1Gi", "1Mi"));
        // Ti + Gi
        assertEquals("1.0009765625Ti", QuantityUtils.add("1Ti", "1Gi"));
        // Pi + Ti
        assertEquals("1.0009765625Pi", QuantityUtils.add("1Pi", "1Ti"));
        // Ei + Pi
        assertEquals("1.0009765625Ei", QuantityUtils.add("1Ei", "1Pi"));
        // k + k
        assertEquals("2k", QuantityUtils.add("1k", "1k"));
        // M + k
        assertEquals("1.001M", QuantityUtils.add("1M", "1k"));
        // G + M
        assertEquals("1.001G", QuantityUtils.add("1G", "1M"));
        // T + G
        assertEquals("1.001T", QuantityUtils.add("1T", "1G"));
        // P + T
        assertEquals("1.001P", QuantityUtils.add("1P", "1T"));
        // E + P
        assertEquals("1.001E", QuantityUtils.add("1E", "1P"));
        // 无单位（字节）
        assertEquals("3", QuantityUtils.add("1", "2"));
    }

    @Test
    void testAddCpuUnits() {
        // m + m
        assertEquals("2m", QuantityUtils.add("1m", "1m"));
        // 1 + 1m
        assertEquals("1.001", QuantityUtils.add("1", "1m"));
        // 0.5 + 500m
        assertEquals("1", QuantityUtils.add("0.5", "500m"));
        // 0.1 + 0.2
        assertEquals("0.3", QuantityUtils.add("0.1", "0.2"));
        // 1 + 2
        assertEquals("3", QuantityUtils.add("1", "2"));
        // 0.25 + 0.25
        assertEquals("0.5", QuantityUtils.add("0.25", "0.25"));
        // 100m + 0.1
        assertEquals("200m", QuantityUtils.add("100m", "0.1"));
    }
} 