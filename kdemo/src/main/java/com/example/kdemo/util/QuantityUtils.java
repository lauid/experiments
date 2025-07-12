package com.example.kdemo.util;

import io.kubernetes.client.custom.Quantity;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuantityUtils {
    // 内存单位（字节为基准）
    private static final String[] MEMORY_UNITS = {"Ei", "Pi", "Ti", "Gi", "Mi", "Ki", "E", "P", "T", "G", "M", "k"};
    private static final long[] MEMORY_MULTIPLIERS = {
            1024L*1024*1024*1024*1024*1024, // Ei
            1024L*1024*1024*1024*1024,      // Pi
            1024L*1024*1024*1024,           // Ti
            1024L*1024*1024,                // Gi
            1024L*1024,                     // Mi
            1024L,                          // Ki
            1000000000000000000L,           // E
            1000000000000000L,              // P
            1000000000000L,                 // T
            1000000000L,                    // G
            1000000L,                       // M
            1000L                           // k
    };
    // CPU 单位
    private static final String CPU_M = "m";

    private static final Pattern PATTERN = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)([a-zA-Z]*)$");

    /**
     * 相加多个 Kubernetes Quantity 字符串，输出为与第一个参数相同的单位。
     * 支持内存（如 Mi、Gi）和 CPU（如 m、无单位=核）。
     */
    public static String add(String... quantities) {
        if (quantities == null || quantities.length == 0) return "0";
        String first = quantities[0];
        String unit = extractUnit(first);
        boolean isCpu = isCpuUnit(unit);
        BigDecimal sum = BigDecimal.ZERO;
        for (String q : quantities) {
            sum = sum.add(parseToBase(q, isCpu));
        }
        return formatFromBase(sum, unit, isCpu);
    }

    // 提取单位
    private static String extractUnit(String s) {
        Matcher m = PATTERN.matcher(s.trim());
        if (m.matches()) {
            return m.group(2);
        }
        return "";
    }

    // 判断是否为 CPU 单位
    private static boolean isCpuUnit(String unit) {
        return CPU_M.equals(unit) || "".equals(unit);
    }

    // 解析为基础单位（内存=字节，CPU=核的千分之一）
    private static BigDecimal parseToBase(String s, boolean isCpu) {
        Matcher m = PATTERN.matcher(s.trim());
        if (!m.matches()) throw new IllegalArgumentException("Invalid quantity: " + s);
        BigDecimal value = new BigDecimal(m.group(1));
        String unit = m.group(2);
        if (isCpu) {
            if (CPU_M.equals(unit)) {
                // m 单位，千分之一核
                return value;
            } else {
                // 无单位，直接视为核，转为 m
                return value.multiply(BigDecimal.valueOf(1000));
            }
        } else {
            // 内存单位
            for (int i = 0; i < MEMORY_UNITS.length; i++) {
                if (MEMORY_UNITS[i].equals(unit)) {
                    return value.multiply(BigDecimal.valueOf(MEMORY_MULTIPLIERS[i]));
                }
            }
            // 无单位，视为字节
            return value;
        }
    }

    // 从基础单位格式化为目标单位
    private static String formatFromBase(BigDecimal base, String unit, boolean isCpu) {
        if (isCpu) {
            if (CPU_M.equals(unit)) {
                // 输出 m
                return base.stripTrailingZeros().toPlainString() + "m";
            } else {
                // 输出核
                return base.divide(BigDecimal.valueOf(1000)).stripTrailingZeros().toPlainString();
            }
        } else {
            for (int i = 0; i < MEMORY_UNITS.length; i++) {
                if (MEMORY_UNITS[i].equals(unit)) {
                    BigDecimal v = base.divide(BigDecimal.valueOf(MEMORY_MULTIPLIERS[i]));
                    return v.stripTrailingZeros().toPlainString() + unit;
                }
            }
            // 默认输出字节
            return base.stripTrailingZeros().toPlainString();
        }
    }

    // 支持直接传 Quantity
    public static String addQuantities(Quantity... quantities) {
        String[] arr = new String[quantities.length];
        for (int i = 0; i < quantities.length; i++) {
            arr[i] = quantities[i].toSuffixedString();
        }
        return add(arr);
    }
} 