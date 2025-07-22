package com.example.kdemo.util;

import io.kubernetes.client.custom.Quantity;
import java.math.BigDecimal;

public class QuantityUtils {
    /**
     * 完全对齐Go官方Quantity行为：
     * - 所有输入用Quantity解析
     * - 加法后用Quantity.toSuffixedString()输出
     */
    public static String add(String... quantities) {
        if (quantities == null || quantities.length == 0) return "0";
        Quantity sum = new Quantity("0");
        for (String q : quantities) {
            Quantity qObj = new Quantity(q);
            sum = new Quantity(sum.getNumber().add(qObj.getNumber()), sum.getFormat());
        }
        return sum.toSuffixedString();
    }

    /**
     * 完全对齐Go官方Quantity行为：支持直接传 Quantity
     */
    public static String addQuantities(Quantity... quantities) {
        if (quantities == null || quantities.length == 0) return "0";
        Quantity sum = new Quantity("0");
        for (Quantity q : quantities) {
            sum = new Quantity(sum.getNumber().add(q.getNumber()), sum.getFormat());
        }
        return sum.toSuffixedString();
    }

    /**
     * 智能格式化为最合适单位（如kubectl展示风格）。
     * @param value 基础单位（字节或m）
     * @param isCpu 是否为CPU（true: m为基础单位，false: 字节）
     * @return 最合适单位的字符串
     */
    public static String formatBestUnit(String value, boolean isCpu) {
        try {
            if (isCpu) {
                // CPU: m为基础单位
                BigDecimal milli = new BigDecimal(value);
                if (milli.abs().compareTo(BigDecimal.valueOf(1000)) >= 0) {
                    BigDecimal v = milli.divide(BigDecimal.valueOf(1000), 6, java.math.RoundingMode.HALF_UP);
                    return v.stripTrailingZeros().toPlainString();
                } else {
                    return milli.stripTrailingZeros().toPlainString() + "m";
                }
            } else {
                // 内存/存储: 字节为基础单位
                String[] units = {"Ei", "Pi", "Ti", "Gi", "Mi", "Ki"};
                long[] factors = {
                    1024L*1024*1024*1024*1024*1024,
                    1024L*1024*1024*1024*1024,
                    1024L*1024*1024*1024,
                    1024L*1024*1024,
                    1024L*1024,
                    1024L
                };
                BigDecimal bytes = new BigDecimal(value);
                for (int i = 0; i < units.length; i++) {
                    BigDecimal factor = BigDecimal.valueOf(factors[i]);
                    if (bytes.abs().compareTo(factor) >= 0) {
                        BigDecimal v = bytes.divide(factor, 6, java.math.RoundingMode.HALF_UP);
                        if (v.stripTrailingZeros().scale() <= 0) {
                            return v.stripTrailingZeros().toPlainString() + units[i];
                        } else {
                            return v.stripTrailingZeros().toPlainString() + units[i];
                        }
                    }
                }
                // 小于1Ki，直接输出字节
                return bytes.stripTrailingZeros().toPlainString();
            }
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 总是带单位的智能格式化（如kubectl展示风格，最小单位为Ki或m）
     * @param value 基础单位（字节或m）
     * @param isCpu 是否为CPU（true: m为基础单位，false: 字节）
     * @return 总是带单位的字符串
     */
    public static String formatBestUnitAlwaysWithSuffix(String value, boolean isCpu) {
        try {
            if (isCpu) {
                BigDecimal milli = new BigDecimal(value);
                if (milli.abs().compareTo(BigDecimal.valueOf(1000)) >= 0) {
                    BigDecimal v = milli.divide(BigDecimal.valueOf(1000), 6, java.math.RoundingMode.HALF_UP);
                    return v.stripTrailingZeros().toPlainString(); // 1核及以上直接输出小数
                } else {
                    return milli.stripTrailingZeros().toPlainString() + "m"; // 小于1核输出m
                }
            } else {
                String[] units = {"Ei", "Pi", "Ti", "Gi", "Mi", "Ki"};
                long[] factors = {
                    1024L*1024*1024*1024*1024*1024,
                    1024L*1024*1024*1024*1024,
                    1024L*1024*1024*1024,
                    1024L*1024*1024,
                    1024L*1024,
                    1024L
                };
                BigDecimal bytes = new BigDecimal(value);
                for (int i = 0; i < units.length; i++) {
                    BigDecimal factor = BigDecimal.valueOf(factors[i]);
                    if (bytes.abs().compareTo(factor) >= 0) {
                        BigDecimal v = bytes.divide(factor, 6, java.math.RoundingMode.HALF_UP);
                        return v.stripTrailingZeros().toPlainString() + units[i];
                    }
                }
                // 小于1Ki，强制输出1Ki
                return "1Ki";
            }
        } catch (Exception e) {
            return value;
        }
    }
} 