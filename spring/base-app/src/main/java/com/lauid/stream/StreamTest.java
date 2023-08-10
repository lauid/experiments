package com.lauid.stream;

import java.util.*;
import java.util.stream.Stream;

public class StreamTest {
    public static void main(String[] args) {
        //1, list集合的stream流
        List<String> names = new ArrayList<>();
        Collections.addAll(names, "张三丰", "张无忌", "赵敏");
        Stream<String> stream = names.stream();

        //2,set集合的stream流
        HashSet<String> sets = new HashSet<>();
        Collections.addAll(sets, "刘德华", "张曼玉", "吴京");
        Stream<String> stream1 = sets.stream();
        stream1.filter(a -> a.contains("刘")).filter((b) -> {
            return b.length() == 3;
        }).forEach(System.out::println);

        //3,map集合的stream流
        HashMap<String, Double> map1 = new HashMap<>();
        map1.put("古力娜扎", 183.10);
        map1.put("迪丽热巴", 173.10);
        map1.put("玛尔扎哈", 178.14);

        Set<String> keys = map1.keySet();
        Stream<String> ks = keys.stream();

        Collection<Double> values = map1.values();
        Stream<Double> vs = values.stream();

        Set<Map.Entry<String, Double>> entries = map1.entrySet();
        Stream<Map.Entry<String, Double>> kvs = entries.stream();
        kvs.filter(a -> a.getKey().contains("巴")).forEach(b -> System.out.println(b.getKey() + "===>" + b.getValue()));
        
        //4,获取数组的stream流
        String[] names2 = {"张翠山","东方不白","孤独秋容","北乔峰","南慕容"};
        Stream<String> stream2 = Arrays.stream(names2);
        Stream<String> names21 = Stream.of(names2);
    }
}
