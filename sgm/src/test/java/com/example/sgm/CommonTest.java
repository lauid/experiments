package com.example.sgm;

import com.example.sang.MyConfig;
import com.example.sang.ScopeTest;
import com.example.sgm.service.HelloService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
public class CommonTest {
    @Autowired
    private HelloService helloService;

    @Test
    public void sayHello() {
        helloService.sayHello("Tom");
    }

    @Test
    public void testArrayList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        System.out.println("ArrayLIst:" + list);

        list.remove("A");
        System.out.println("after remove:" + list);

        System.out.println("size of ArrayList:" + list.size());
        System.out.println("Elem at index 0:" + list.get(0));
    }

    @Test
    public void testHashSet() {
        HashSet<String> set = new HashSet<>();

        set.add("A");
        set.add("B");
        set.add("C");
        System.out.println("HashSet:" + set);

        set.remove("A");
        System.out.println("After remove:" + set);
        System.out.println("size of HashSet:" + set.size());
        System.out.println("Contains 'C'?:" + set.contains("C"));
    }

    @Test
    public void testHashMap() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "A");
        map.put(2, "B");
        map.put(3, "C");
        map.put(4, "D");
        System.out.println("HashMap:" + map);

        map.remove(2);
        System.out.println("After remove:" + map);
        System.out.println("size of HashSet:" + map.size());
        System.out.println("value for key 1：" + map.get(1));

        for (Integer key : map.keySet()) {
            System.out.println(key + ":" + map.get(key));
        }

        map.forEach((key, value) -> System.out.println(key + ":" + value));

        Assertions.assertEquals(3, map.size());
    }

    @Test
    public void testHashTable() {
        Hashtable<Integer, String> table = new Hashtable<>();

        table.put(1, "Apple");
        table.put(2, "banana");
        table.put(3, "orange");

        System.out.println("key 2 value:" + table.get(2));
        System.out.println("table size" + table.size());
        for (Integer key : table.keySet()) {
            System.out.println(key + ":" + table.get(key));
        }

        table.forEach((key, value) -> System.out.println(key + ":" + value));
    }

    @Test
    public void testConcurrentHashMap() {
        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();
        map.put(1, "A");
        map.put(2, "B");
        map.put(3, "C");

        System.out.println("key 2 value:" + map.get(1));
        System.out.println("map size" + map.size());

        for (Integer key : map.keySet()) {
            System.out.println(key + ":" + map.get(key));
        }

        map.forEach((key, value) -> System.out.println(key + ":" + value));
    }

    @Test
    public void testArrayTraversal() {
        List<String> list = new ArrayList<String>();
        list.add("Hello");
        list.add("World");
        list.add("HAHAHAHA");
        //第一种遍历方法使用 For-Each 遍历 List
        for (String str : list) {            //也可以改写 for(int i=0;i<list.size();i++) 这种形式
            System.out.println(str);
        }

        //第二种遍历，把链表变为数组相关的内容进行遍历
        String[] strArray = new String[list.size()];
        list.toArray(strArray);
        for (int i = 0; i < strArray.length; i++) //这里也可以改写为  for(String str:strArray) 这种形式
        {
            System.out.println(strArray[i]);
        }

        //第三种遍历 使用迭代器进行相关遍历

        Iterator<String> ite = list.iterator();
        while (ite.hasNext())//判断下一个元素之后有值
        {
            System.out.println(ite.next());
        }
    }

    @Test
    public void TestScope() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        ScopeTest bean1 = context.getBean(ScopeTest.class);
        ScopeTest bean2 = context.getBean(ScopeTest.class);
        Assertions.assertNotEquals(bean1, bean2);
        context.close();
    }
}
