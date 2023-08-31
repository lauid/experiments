package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.example.demo.spi.LoggerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
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
import java.util.stream.Collectors;

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

    /**
     * Class<?>[] 是一个 Java 中的泛型数组表示形式。其中，Class<?> 表示一个未知类型的 Class 对象，[] 表示这是一个数组，而 <?> 则表示未知的通配符类型。
     */
    @Test
    public void testClass() {
        Class<?>[] classes = new Class<?>[3];
        classes[0] = Integer.class;
        classes[1] = String.class;
        classes[2] = Boolean.class;
        System.out.println(Arrays.asList(classes));
    }

    public enum Gender {
        MALE("man","男性"), FEMALE("woman","女性");

        private final String name;
        private String code;

        Gender(String code, String name ) {
            this.code = code;
            this.name = name;
        }
    }

    @Test
    public void testEnum() {
        Gender gender = Gender.MALE;
        System.out.println(gender.name);
    }

    public void testList2String(){
        List<Long> longs = new ArrayList<>();
        longs.add(1L);
        longs.add(2L);
        longs.add(3L);
        String actual = JSON.toJSONString(longs);
        System.out.println(actual);
    }

    public enum ProductSPU{
        STATUS_A("1"),STATUS_B("4");

        private final String status;

        ProductSPU(String number) {
            this.status = number;
        }

        public String getStatus() {
            return status;
        }

        public Integer getStatus1() {
            return Integer.parseInt(this.status);
        }

        public static boolean getStatus2(String a) {
            return a.compareTo("3") > 0;
        }
    }

    @Test
    public void testStream() {
        double[] array1 = Arrays.stream(ProductSPU.values()).mapToDouble(a -> {
            return Integer.parseInt(a.getStatus()) + 10;
        }).toArray();
        System.out.println(Arrays.toString(array1));

        int[] array = Arrays.stream(ProductSPU.values()).mapToInt(ProductSPU::getStatus1).toArray();
        System.out.println(Arrays.toString(array));
    }
}