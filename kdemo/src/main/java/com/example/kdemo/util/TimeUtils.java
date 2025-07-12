package com.example.kdemo.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 时间格式转换工具类
 */
public class TimeUtils {
    
    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MILLISECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 将日期时间字符串转换为时间戳（秒）
     * 支持格式：
     * - "2024-01-01 12:30:45"
     * - "2024-01-01T12:30:45Z"
     * - "2024-01-01 12:30:45.123"
     * - "1640995200" (纯数字时间戳)
     * 
     * @param dateTimeStr 日期时间字符串
     * @return 时间戳（秒）
     * @throws IllegalArgumentException 如果输入为空或格式无效
     */
    public static Long parseTimeParameter(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 如果是纯数字，认为是时间戳
            if (dateTimeStr.matches("^\\d+$")) {
                long timestamp = Long.parseLong(dateTimeStr);
                // 如果大于等于 10^12，认为是毫秒级时间戳，需要转换为秒
                if (timestamp >= 1_000_000_000_000L) {
                    return timestamp / 1000;
                }
                // 否则认为是秒级时间戳，直接返回
                return timestamp;
            }
            
            // 尝试解析标准格式 "2024-01-01 12:30:45"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, STANDARD_FORMATTER);
                return localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
            }
            
            // 尝试解析RFC3339格式 "2024-01-01T12:30:45Z"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
                Instant instant = Instant.parse(dateTimeStr);
                return instant.getEpochSecond();
            }
            
            // 尝试解析带毫秒的格式 "2024-01-01 12:30:45.123"
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, MILLISECOND_FORMATTER);
                return localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
            }
            
            // 如果都不匹配，尝试直接解析为Instant
            Instant instant = Instant.parse(dateTimeStr);
            return instant.getEpochSecond();
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的时间戳格式: " + dateTimeStr, e);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("无法解析日期时间格式: " + dateTimeStr, e);
        }
    }
    
    /**
     * 将Instant转换为时间戳（秒）
     * 
     * @param instant Instant对象
     * @return 时间戳（秒）
     */
    public static Long instantToTimestamp(Instant instant) {
        return instant != null ? instant.getEpochSecond() : null;
    }
    
    /**
     * 将LocalDateTime转换为时间戳（秒）
     * 
     * @param localDateTime LocalDateTime对象
     * @param zoneId 时区ID
     * @return 时间戳（秒）
     */
    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null || zoneId == null) {
            return null;
        }
        return localDateTime.atZone(zoneId).toEpochSecond();
    }
    
    /**
     * 将时间戳转换为日期时间字符串
     * 
     * @param timestamp 时间戳（秒）
     * @param zoneId 时区ID
     * @return 日期时间字符串 "yyyy-MM-dd HH:mm:ss"
     */
    public static String timestampToDateTimeString(Long timestamp, ZoneId zoneId) {
        if (timestamp == null || zoneId == null) {
            return null;
        }
        
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.format(STANDARD_FORMATTER);
    }
    
    /**
     * 将时间戳转换为UTC日期时间字符串
     * 
     * @param timestamp 时间戳（秒）
     * @return UTC日期时间字符串
     */
    public static String timestampToUtcString(Long timestamp) {
        return timestampToDateTimeString(timestamp, ZoneId.of("UTC"));
    }
    
    /**
     * 获取当前时间戳（秒）
     * 
     * @return 当前时间戳（秒）
     */
    public static Long currentTimestamp() {
        return Instant.now().getEpochSecond();
    }
    
    /**
     * 获取指定时区的当前时间戳（秒）
     * 
     * @param zoneId 时区ID
     * @return 当前时间戳（秒）
     */
    public static Long currentTimestamp(ZoneId zoneId) {
        return LocalDateTime.now(zoneId).atZone(zoneId).toEpochSecond();
    }
    
    /**
     * 检查时间戳是否为毫秒级
     * 
     * @param timestamp 时间戳
     * @return true如果是毫秒级，false如果是秒级
     */
    public static boolean isMillisecondTimestamp(long timestamp) {
        return timestamp >= 1_000_000_000_000L;
    }
    
    /**
     * 将毫秒级时间戳转换为秒级时间戳
     * 
     * @param millisecondTimestamp 毫秒级时间戳
     * @return 秒级时间戳
     */
    public static Long millisecondToSecondTimestamp(Long millisecondTimestamp) {
        return millisecondTimestamp != null ? millisecondTimestamp / 1000 : null;
    }
    
    /**
     * 将秒级时间戳转换为毫秒级时间戳
     * 
     * @param secondTimestamp 秒级时间戳
     * @return 毫秒级时间戳
     */
    public static Long secondToMillisecondTimestamp(Long secondTimestamp) {
        return secondTimestamp != null ? secondTimestamp * 1000 : null;
    }
} 