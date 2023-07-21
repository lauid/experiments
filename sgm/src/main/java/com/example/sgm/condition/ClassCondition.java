package com.example.sgm.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class ClassCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionOnClass.class.getName());
        String[] value = (String[]) annotationAttributes.get("value");

        try {
            for (String className: value){
                Class<?> cls = Class.forName("redis.clients.jedis.Jedis");
            }
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
            return false;
        }
        return true;

    }
}
