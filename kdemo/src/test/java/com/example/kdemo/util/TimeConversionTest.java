package com.example.kdemo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 时间格式转换测试类
 */
@DisplayName("时间格式转换测试")
class TimeConversionTest {

    @Test
    @DisplayName("测试年月日时分秒字符串转换为时间戳")
    void testDateTimeStringToTimestamp() {
        // 测试用例1：标准格式 "2024-01-01 12:30:45"
        String dateTimeStr1 = "2024-01-01 12:30:45";
        long expectedTimestamp1 = 1704111045L; // 2024-01-01 12:30:45 UTC
        
        long actualTimestamp1 = convertDateTimeStringToTimestamp(dateTimeStr1);
        assertEquals(expectedTimestamp1, actualTimestamp1, "标准格式转换失败");
        
        // 测试用例2：带时区的格式 "2024-01-01T12:30:45Z"
        String dateTimeStr2 = "2024-01-01T12:30:45Z";
        long expectedTimestamp2 = 1704111045L;
        
        long actualTimestamp2 = convertDateTimeStringToTimestamp(dateTimeStr2);
        assertEquals(expectedTimestamp2, actualTimestamp2, "RFC3339格式转换失败");
        
        // 测试用例3：带毫秒的格式 "2024-01-01 12:30:45.123"
        String dateTimeStr3 = "2024-01-01 12:30:45.123";
        long expectedTimestamp3 = 1704111045L; // 忽略毫秒部分
        
        long actualTimestamp3 = convertDateTimeStringToTimestamp(dateTimeStr3);
        assertEquals(expectedTimestamp3, actualTimestamp3, "带毫秒格式转换失败");
    }
    
    @Test
    @DisplayName("测试Instant转换为时间戳")
    void testInstantToTimestamp() {
        // 测试用例1：固定时间点
        Instant instant1 = Instant.parse("2024-01-01T12:30:45Z");
        long expectedTimestamp1 = 1704111045L;
        
        long actualTimestamp1 = convertInstantToTimestamp(instant1);
        assertEquals(expectedTimestamp1, actualTimestamp1, "Instant转换失败");
        
        // 测试用例2：当前时间
        Instant now = Instant.now();
        long expectedTimestamp2 = now.getEpochSecond();
        
        long actualTimestamp2 = convertInstantToTimestamp(now);
        assertEquals(expectedTimestamp2, actualTimestamp2, "当前时间转换失败");
    }
    
    @Test
    @DisplayName("测试LocalDateTime转换为时间戳")
    void testLocalDateTimeToTimestamp() {
        // 测试用例1：指定时区
        LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 1, 12, 30, 45);
        ZoneId zoneId = ZoneId.of("UTC");
        
        long expectedTimestamp = 1704111045L;
        long actualTimestamp = convertLocalDateTimeToTimestamp(localDateTime, zoneId);
        assertEquals(expectedTimestamp, actualTimestamp, "LocalDateTime转换失败");
        
        // 测试用例2：北京时区
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
        long expectedTimestampBeijing = 1704081045L; // 比UTC早8小时
        long actualTimestampBeijing = convertLocalDateTimeToTimestamp(localDateTime, beijingZone);
        assertEquals(expectedTimestampBeijing, actualTimestampBeijing, "北京时区转换失败");
    }
    
    @Test
    @DisplayName("测试时间戳转换为日期时间字符串")
    void testTimestampToDateTimeString() {
        long timestamp = 1704111045L;
        
        // 转换为UTC时间字符串
        String expectedUtcString = "2024-01-01 12:30:45";
        String actualUtcString = convertTimestampToDateTimeString(timestamp, ZoneId.of("UTC"));
        assertEquals(expectedUtcString, actualUtcString, "时间戳转UTC字符串失败");
        
        // 转换为北京时间字符串
        String expectedBeijingString = "2024-01-01 20:30:45";
        String actualBeijingString = convertTimestampToDateTimeString(timestamp, ZoneId.of("Asia/Shanghai"));
        assertEquals(expectedBeijingString, actualBeijingString, "时间戳转北京时间字符串失败");
    }
    
    @Test
    @DisplayName("测试异常情况")
    void testExceptionCases() {
        // 测试空字符串
        assertThrows(IllegalArgumentException.class, () -> {
            convertDateTimeStringToTimestamp("");
        }, "空字符串应该抛出异常");
        
        // 测试null
        assertThrows(IllegalArgumentException.class, () -> {
            convertDateTimeStringToTimestamp(null);
        }, "null应该抛出异常");
        
        // 测试无效格式
        assertThrows(DateTimeParseException.class, () -> {
            convertDateTimeStringToTimestamp("invalid-date");
        }, "无效格式应该抛出异常");
    }
    
    @Test
    @DisplayName("测试边界情况")
    void testBoundaryCases() {
        // 测试Unix纪元时间
        String epochTime = "1970-01-01 00:00:00";
        long expectedEpochTimestamp = 0L;
        long actualEpochTimestamp = convertDateTimeStringToTimestamp(epochTime);
        assertEquals(expectedEpochTimestamp, actualEpochTimestamp, "Unix纪元时间转换失败");
        
        // 测试未来时间
        String futureTime = "2030-12-31 23:59:59";
        long futureTimestamp = convertDateTimeStringToTimestamp(futureTime);
        assertTrue(futureTimestamp > 1704111045L, "未来时间应该大于当前测试时间");
    }
    
    /**
     * 将日期时间字符串转换为时间戳（秒）
     * 支持格式：
     * - "2024-01-01 12:30:45"
     * - "2024-01-01T12:30:45Z"
     * - "2024-01-01 12:30:45.123"
     */
    private long convertDateTimeStringToTimestamp(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期时间字符串不能为空");
        }
        
        try {
            // 尝试解析标准格式 "2024-01-01 12:30:45"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
                return localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
            }
            
            // 尝试解析RFC3339格式 "2024-01-01T12:30:45Z"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                Instant instant = Instant.parse(dateTimeStr);
                return instant.getEpochSecond();
            }
            
            // 尝试解析带毫秒的格式 "2024-01-01 12:30:45.123"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
                return localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
            }
            
            // 如果都不匹配，尝试直接解析为Instant
            Instant instant = Instant.parse(dateTimeStr);
            return instant.getEpochSecond();
            
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("无法解析日期时间格式: " + dateTimeStr, dateTimeStr, 0);
        }
    }
    
    /**
     * 将Instant转换为时间戳（秒）
     */
    private long convertInstantToTimestamp(Instant instant) {
        return instant.getEpochSecond();
    }
    
    /**
     * 将LocalDateTime转换为时间戳（秒）
     */
    private long convertLocalDateTimeToTimestamp(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId).toEpochSecond();
    }
    
    /**
     * 将时间戳转换为日期时间字符串
     */
    private String convertTimestampToDateTimeString(long timestamp, ZoneId zoneId) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return zonedDateTime.format(formatter);
    }
} 