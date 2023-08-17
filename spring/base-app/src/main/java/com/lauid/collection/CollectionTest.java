package com.lauid.collection;

import com.lauid.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionTest {
    public static void main1(String[] args) {
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

    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setAge(11);
        user1.setName("one");
        user1.setWeight(BigDecimal.valueOf(11.11));
        user1.setId(11);
        users.add(user1);
        User user2 = new User();
        user2.setAge(12);
        user2.setName("two");
        user2.setWeight(BigDecimal.valueOf(12.11));
        user2.setId(12);
        users.add(user2);
        User user3 = new User();
        user3.setAge(13);
        user3.setName("three");
        user3.setWeight(BigDecimal.valueOf(13.11));
        user3.setId(13);
        users.add(user3);

        List<String> list = users.stream().map(User::getName).toList();
        System.out.println(list);


    }
}
