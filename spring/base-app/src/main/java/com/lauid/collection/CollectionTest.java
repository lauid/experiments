package com.lauid.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionTest {
    public static void main(String[] args) {
        ArrayList<String> names = new ArrayList<>();
        Collections.addAll(names, "张三丰", "张无忌", "赵敏");
        System.out.println(names);

        List<String> list = new ArrayList<>();
        list = names.stream().filter(a -> a.startsWith("张")).filter(b -> b.length() == 3).collect(Collectors.toList());
        System.out.println(list);

//        List<String> list2 = new ArrayList<>();
//        list2 = Stream.of(names).filter(a -> a.contains("张")).filter(b -> b.size() == 3).toList();
//        System.out.println(list2);


    }
}
