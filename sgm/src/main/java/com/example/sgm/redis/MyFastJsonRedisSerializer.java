package com.example.sgm.redis;

import org.apache.commons.lang3.SerializationException;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Type;

// 自定义序列化方式
public class MyFastJsonRedisSerializer<T> implements RedisSerializer<T> {

    private final Type type;

    public MyFastJsonRedisSerializer(Type type) {
        this.type = type;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return JSON.toJSONBytes(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return JSON.parseObject(bytes, type);
    }
}

