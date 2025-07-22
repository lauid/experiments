package com.example.kdemo.util;

import io.kubernetes.client.custom.Quantity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuantityUtilsTest {

    @Test
    void testAddMemory() {
        String result = QuantityUtils.add("500Mi", "1Gi");
        assertEquals("1572864000", result); // Go风格：输出为字节数
    }

    @Test
    void testAddCpu() {
        String result = QuantityUtils.add("500m", "1");
        assertEquals("1500m", result); // Go风格：输出为m
    }

    @Test
    void testAddQuantitiesCpu() {
        Quantity q1 = new Quantity("250m");
        Quantity q2 = new Quantity("0.75");
        String result = QuantityUtils.addQuantities(q1, q2);
        assertEquals("1000m", result); // Go风格：输出为m
    }

    @Test
    void testAddDifferentMemoryUnits() {
        String result = QuantityUtils.add("1Gi", "1024Mi");
        assertEquals("2147483648", result); // 1Gi+1024Mi=2Gi=2147483648字节
    }

    @Test
    void testInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> QuantityUtils.add("abc", "1Gi"));
    }

    @Test
    void testAddMemoryUnits() {
        // Ki + Ki
        assertEquals("2048", QuantityUtils.add("1Ki", "1Ki"));
        // Mi + Ki
        assertEquals("1049600", QuantityUtils.add("1Mi", "1Ki"));
        // Gi + Mi
        assertEquals("1074790400", QuantityUtils.add("1Gi", "1Mi"));
        // Ti + Gi
        assertEquals("1099512676352", QuantityUtils.add("1Ti", "1Gi"));
        // Pi + Ti
        assertEquals("1125899906842624", QuantityUtils.add("1Pi", "1Ti"));
        // Ei + Pi
        assertEquals("1152921504606846976", QuantityUtils.add("1Ei", "1Pi"));
        // k + k
        assertEquals("2000", QuantityUtils.add("1k", "1k"));
        // M + k
        assertEquals("1001000", QuantityUtils.add("1M", "1k"));
        // G + M
        assertEquals("1001000000", QuantityUtils.add("1G", "1M"));
        // T + G
        assertEquals("1001000000000", QuantityUtils.add("1T", "1G"));
        // P + T
        assertEquals("1001000000000000", QuantityUtils.add("1P", "1T"));
        // E + P
        assertEquals("1001000000000000000", QuantityUtils.add("1E", "1P"));
        // 无单位（字节）
        assertEquals("3", QuantityUtils.add("1", "2"));
    }

    @Test
    void testAddCpuUnits() {
        // m + m
        assertEquals("2m", QuantityUtils.add("1m", "1m"));
        // 1 + 1m
        assertEquals("1001m", QuantityUtils.add("1", "1m"));
        // 0.5 + 500m
        assertEquals("1000m", QuantityUtils.add("0.5", "500m"));
        // 0.1 + 0.2
        assertEquals("300m", QuantityUtils.add("0.1", "0.2"));
        // 1 + 2
        assertEquals("3", QuantityUtils.add("1", "2"));
        // 0.25 + 0.25
        assertEquals("500m", QuantityUtils.add("0.25", "0.25"));
        // 100m + 0.1
        assertEquals("200m", QuantityUtils.add("100m", "0.1"));
    }

    @Test
    void testAddStorageUnits() {
        // 1000字节+1k=2000字节
        assertEquals("2000", QuantityUtils.add("1000", "1k"));
        // 1M+1k=1001000字节
        assertEquals("1001000", QuantityUtils.add("1M", "1k"));
        // 1G+1M=1001000000字节
        assertEquals("1001000000", QuantityUtils.add("1G", "1M"));
        // 1T+1G=1001000000000字节
        assertEquals("1001000000000", QuantityUtils.add("1T", "1G"));
        // 1P+1T=1001000000000000字节
        assertEquals("1001000000000000", QuantityUtils.add("1P", "1T"));
        // 1E+1P=1001000000000000000字节
        assertEquals("1001000000000000000", QuantityUtils.add("1E", "1P"));
        // 混合二进制和十进制单位
        assertEquals("1049600", QuantityUtils.add("1M", "1Ki"));
        assertEquals("1073742848", QuantityUtils.add("1G", "1Ki"));
    }

    // 移除testFormatMemory和testFormatCpu方法，只保留testFormatBestUnitMemory和testFormatBestUnitCpu

    @Test
    void testFormatBestUnitMemory() {
        // 小于1Ki
        assertEquals("999", QuantityUtils.formatBestUnit("999", false));
        // 1Ki
        assertEquals("1Ki", QuantityUtils.formatBestUnit("1024", false));
        // 1536字节=1.5Ki
        assertEquals("1.5Ki", QuantityUtils.formatBestUnit("1536", false));
        // 1Mi
        assertEquals("1Mi", QuantityUtils.formatBestUnit("1048576", false));
        // 1.5Mi
        assertEquals("1.5Mi", QuantityUtils.formatBestUnit("1572864", false));
        // 1Gi
        assertEquals("1Gi", QuantityUtils.formatBestUnit("1073741824", false));
        // 1.5Gi
        assertEquals("1.5Gi", QuantityUtils.formatBestUnit("1610612736", false));
        // 1Ti
        assertEquals("1Ti", QuantityUtils.formatBestUnit("1099511627776", false));
        // 1Pi
        assertEquals("1Pi", QuantityUtils.formatBestUnit("1125899906842624", false));
        // 1Ei
        assertEquals("1Ei", QuantityUtils.formatBestUnit("1152921504606846976", false));
        // 1000字节（十进制单位，仍优先二进制）
        assertEquals("1000", QuantityUtils.formatBestUnit("1000", false));
        // 1000000字节
        assertEquals("976.5625Ki", QuantityUtils.formatBestUnit("1000000", false));
        // 负数
        assertEquals("-1.5Gi", QuantityUtils.formatBestUnit("-1610612736", false));
    }

    @Test
    void testFormatBestUnitCpu() {
        // 小于1核
        assertEquals("999m", QuantityUtils.formatBestUnit("999", true));
        // 1核
        assertEquals("1", QuantityUtils.formatBestUnit("1000", true));
        // 1.5核
        assertEquals("1.5", QuantityUtils.formatBestUnit("1500", true));
        // 0.1核
        assertEquals("100m", QuantityUtils.formatBestUnit("100", true));
        // 0.25核
        assertEquals("250m", QuantityUtils.formatBestUnit("250", true));
        // 2核
        assertEquals("2", QuantityUtils.formatBestUnit("2000", true));
        // 负数
        assertEquals("-1.5", QuantityUtils.formatBestUnit("-1500", true));
    }

    @Test
    void testFormatBestUnitAlwaysWithSuffixMemory() {
        // 小于1Ki
        assertEquals("1Ki", QuantityUtils.formatBestUnitAlwaysWithSuffix("999", false));
        // 1Ki
        assertEquals("1Ki", QuantityUtils.formatBestUnitAlwaysWithSuffix("1024", false));
        // 1536字节=1.5Ki
        assertEquals("1.5Ki", QuantityUtils.formatBestUnitAlwaysWithSuffix("1536", false));
        // 1Mi
        assertEquals("1Mi", QuantityUtils.formatBestUnitAlwaysWithSuffix("1048576", false));
        // 1.5Mi
        assertEquals("1.5Mi", QuantityUtils.formatBestUnitAlwaysWithSuffix("1572864", false));
        // 1Gi
        assertEquals("1Gi", QuantityUtils.formatBestUnitAlwaysWithSuffix("1073741824", false));
        // 1.5Gi
        assertEquals("1.5Gi", QuantityUtils.formatBestUnitAlwaysWithSuffix("1610612736", false));
        // 1Ti
        assertEquals("1Ti", QuantityUtils.formatBestUnitAlwaysWithSuffix("1099511627776", false));
        // 1Pi
        assertEquals("1Pi", QuantityUtils.formatBestUnitAlwaysWithSuffix("1125899906842624", false));
        // 1Ei
        assertEquals("1Ei", QuantityUtils.formatBestUnitAlwaysWithSuffix("1152921504606846976", false));
        // 1000字节
        assertEquals("1Ki", QuantityUtils.formatBestUnitAlwaysWithSuffix("1000", false));
        // 1000000字节
        assertEquals("976.5625Ki", QuantityUtils.formatBestUnitAlwaysWithSuffix("1000000", false));
        // 负数
        assertEquals("-1.5Gi", QuantityUtils.formatBestUnitAlwaysWithSuffix("-1610612736", false));
    }

    @Test
    void testFormatBestUnitAlwaysWithSuffixCpu() {
        // 小于1核
        assertEquals("999m", QuantityUtils.formatBestUnitAlwaysWithSuffix("999", true));
        // 1核
        assertEquals("1", QuantityUtils.formatBestUnitAlwaysWithSuffix("1000", true));
        // 1.5核
        assertEquals("1.5", QuantityUtils.formatBestUnitAlwaysWithSuffix("1500", true));
        // 0.1核
        assertEquals("100m", QuantityUtils.formatBestUnitAlwaysWithSuffix("100", true));
        // 0.25核
        assertEquals("250m", QuantityUtils.formatBestUnitAlwaysWithSuffix("250", true));
        // 2核
        assertEquals("2", QuantityUtils.formatBestUnitAlwaysWithSuffix("2000", true));
        // 负数
        assertEquals("-1.5", QuantityUtils.formatBestUnitAlwaysWithSuffix("-1500", true));
    }
} 