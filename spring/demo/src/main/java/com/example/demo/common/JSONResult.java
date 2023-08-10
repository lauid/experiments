package com.example.demo.common;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class JSONResult {
//    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Integer code;

    private final String msg;
    private final Object data;

    public static JSONResult build(Integer code, String msg, Object data) {
        return new JSONResult(code, msg, data);
    }

    public static JSONResult success(Object data) {
        return new JSONResult(data);
    }

    public static JSONResult success() {
        return new JSONResult(null);
    }

    public static JSONResult errorMsg(String msg) {
        return new JSONResult(500, msg, null);
    }

    public static JSONResult errorWrap(Object data) {
        return new JSONResult(501, "error", data);
    }

    public static JSONResult errorTokenMsg(String msg) {
        return new JSONResult(502, msg, null);
    }

    public static JSONResult errorException(String msg) {
        return new JSONResult(555, msg, null);
    }

//    public Boolean isOK() {
//        return this.code == 200;
//    }

    public JSONResult(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public JSONResult(Object data) {
        this.code = 200;
        this.msg = "success";
        this.data = data;
    }

//    public static JSONResult formatToPojo(String jsonData, Class<?> clazz) {
//        try {
//            if (clazz == null) {
//                return MAPPER.readValue(jsonData, JSONResult.class);
//            }
//            JsonNode jsonNode = MAPPER.readTree(jsonData);
//            JsonNode data = jsonNode.get("data");
//            Object obj = null;
//            if (data.isObject()) {
//                obj = MAPPER.readValue(data.traverse(), clazz);
//            } else if (data.isTextual()) {
//                obj = MAPPER.readValue(data.asText(), clazz);
//            }
//            return build(jsonNode.get("code").intValue(), jsonNode.get("msg").asText(), obj);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static JSONResult formatToList(String jsonData, Class<?> clazz) {
//        JsonNode jsonNode = null;
//        try {
//            jsonNode = MAPPER.readTree(jsonData);
//            JsonNode data = jsonNode.get("data");
//            Object obj = null;
//            if (data.isArray() && data.size() > 0) {
//                obj = MAPPER.readValue(data.traverse(), MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
//            }
//            return build(jsonNode.get("code").intValue(), jsonNode.get("msg").asText(), obj);
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public static JSONResult format(String json) {
//        try {
//            return MAPPER.readValue(json, JSONResult.class);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }
}
