package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
public class BaseTest {
    @Test
    public void testDecimal() {
        BigDecimal a = new BigDecimal("2");
        BigDecimal b = new BigDecimal("5.5");
        Assertions.assertEquals(b.subtract(a), new BigDecimal("3.5"));
        Assertions.assertEquals(b.add(a), new BigDecimal("7.5"));
        Assertions.assertEquals(b.multiply(a), new BigDecimal("11.0"));
        Assertions.assertEquals(b.divide(a, 2, RoundingMode.HALF_UP), new BigDecimal("2.75"));
    }

    @Test
    public void testReflect() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Class<?> targetClass = Class.forName("com.example.demo.TargetObject");
        TargetObject targetObject = (TargetObject) targetClass.newInstance();
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            System.out.println(declaredMethod.getName());
        }

        /**
         * 获取指定方法并调用
         */
        Method publicMethod = targetClass.getDeclaredMethod("publicMethod", String.class);
        publicMethod.invoke(targetObject, "Guide");

        /**
         * 获取指定参数并对参数进行修改
         */
        Field field = targetClass.getDeclaredField("value");
        field.setAccessible(true);
        field.set(targetObject, "Guide2");


        /**
         * 调用 private 方法
         */
        Method privateMethod = targetClass.getDeclaredMethod("privateMethod");
        //为了调用private方法我们取消安全检查
        privateMethod.setAccessible(true);
        privateMethod.invoke(targetObject);
    }

}