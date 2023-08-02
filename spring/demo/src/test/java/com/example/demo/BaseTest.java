package com.example.demo;

import com.example.demo.spi.LoggerService;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testLogger() {
        LoggerService service = LoggerService.getService();
        service.info("Hello SPI");
        service.debug("Hello SPI");
    }

    @Test
    public void test1() {
        Integer i = 10_000;
        System.out.println(i);

        try (BufferedReader reader = new BufferedReader(new FileReader("c:\\share\\aa.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLambda() {
        List<String> strList1 = new ArrayList<>(Arrays.asList("www.qq.com".split("\\.")));
        strList1.forEach(System.out::println);
        List<String> strList2 = List.of("www", "baidu", "com");
        strList2.forEach(s -> {
            System.out.println(s);
        });
    }

    class GT<T> {
        public static int var = 0;

        public void nothing(T x) {
        }
    }

    @Test
    public void testGeneric() {
        GT<Integer> gts = new GT<Integer>();
        gts.var = 22;

        GT<Integer> gt = new GT<Integer>();
        gt.var = 11;
        System.out.println(gts.var);
    }

    class Person {
        public String name;

        Person(String name) {
            this.name = name;
        }
    }

    private void swap(Person man1, Person man2) {
        Person tmp;
        tmp = man1;
        man1 = man2;
        man2 = tmp;
        System.out.println("man1 name: " + man1.name);
        System.out.println("man2 name: " + man2.name);
    }

    @Test
    public void testSwap() {
        Person man1 = new Person("man1");
        Person man2 = new Person("man2");
        System.out.println("before: " + man1.name + " " + man2.name);
        swap(man1, man2);
        System.out.println("after: " + man1.name + " " + man2.name);
    }
}