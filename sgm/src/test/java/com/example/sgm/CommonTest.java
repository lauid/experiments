package com.example.sgm;

import com.example.sang.MyConfig;
import com.example.sang.ScopeTest;
import com.example.sgm.service.HelloService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Test
    public void test1() {
        List<String> list1 = new ArrayList<>();
        list1.add("A");
        list1.add("B");
        list1.add("C");
        list1.add("F");
        List<String> list2 = new ArrayList<>();
        list2.add("A");
        list2.add("B");
        list2.add("C");
        list2.add("D");

        System.out.println("list1:" + list1);
        list1.remove("A");
        System.out.println("list1 after remove A:" + list1);
        System.out.println("list2:" + list2);

        //交集
        List<String> intersaction = new ArrayList<>(list1);
        intersaction.retainAll(list2);
        System.out.println("交集：" + intersaction);

        //差集
        List<String> diff = new ArrayList<>(list2);
        diff.removeAll(list1);
        System.out.println("差集：" + diff);

        //并集
        List<String> union = new ArrayList<>(list1);
        for (String item : list2) {
            if (!union.contains(item)) {
                union.add(item);
            }
        }
        System.out.println("并集" + union);
    }

    @Test
    public void completableFutureTest() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        future.thenApply(s -> s + " World.")
                .thenAccept(System.out::println);

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> " World.");
        Supplier<String> supplier = () -> "Hello";
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future1, future2);
    }

    @Test
    public void printPair() {
        printPair("A", "a");
        printPair(111, 222);
    }

    private <T, U> void printPair(T first, U second) {
        System.out.println("First:" + first);
        System.out.println("Second:" + second);
    }

    @Test
    public void testEnum() {
        enum Color {RED, GREEN, BLACK}
        for (Color myVar : Color.values()) {
            System.out.println(myVar + " index at " + myVar.ordinal());
        }

        System.out.println(Color.valueOf("RED"));

        Color foo = Color.RED;
        switch (foo) {
            case RED:
                System.out.println("--------red");
                break;
            case GREEN:
                System.out.println("--------green");
                break;
            case BLACK:
                System.out.println("--------black");
                break;
        }
    }

    @Test
    public void testEnum2() {
        enum Color {
            RED, BLACK, GREEN;

            private Color() {
                System.out.println("Construct called for " + this.toString());
            }

            public void colorInfo() {
                System.out.println("Universal color.");
            }
        }

        Color bar = Color.RED;
        bar.colorInfo();
    }

    @Test
    public void testString1() {
        String a = "running ";
        StringBuilder b = new StringBuilder("Hello World.");
        b.append("foo.");
        b.append("bar.");
        b.insert(0, "AA");
        b.delete(1, 2);
//        b.reverse();
        b.replace(1, 2, "BBB");

        System.out.println(a.length());
        System.out.println(a.concat(b.toString()));
    }

    @Test
    public void testCompareString() {
        String str = "HELLO world";
        String otherStr = "hello world";
        Object anotherStr = str;
        Assertions.assertTrue(str.compareTo(otherStr) < 0);
        Assertions.assertTrue(str.compareToIgnoreCase(otherStr) == 0);
        Assertions.assertTrue(str.compareTo(anotherStr.toString()) == 0);
    }

    @Test
    public void testLastIndexOfStr() {
        String strOrg = "Hello World.";
        int index = strOrg.lastIndexOf("Wo");
        Assertions.assertTrue(index != -1);
        Assertions.assertTrue(strOrg.lastIndexOf("BB") == -1);
        Assertions.assertTrue(strOrg.lastIndexOf("H") == 0);

        String strCh = "我爱你";
        Assertions.assertTrue(strCh.lastIndexOf("好") == -1);
        Assertions.assertTrue(strCh.lastIndexOf("爱") == 1);
    }

    @Test
    public void testRemoveChar() {
        String str = "你好，world";
        String newStr = str.replace("w", "AA");
        System.out.println(newStr);
        System.out.println(str.replace("你", ""));
        Assertions.assertTrue(removeAtPos(str, 1).equals("你，world"));
        Assertions.assertTrue(removeAtPos(str, 1).equals(str.replace("好", "")));
    }

    private String removeAtPos(String str, int pos) {
        String n = str.substring(0, pos) + str.substring(pos + 1);
        System.out.println(n);
        return n;
    }

    @Test
    public void testReplace() {
        String str = "hello,world,我爱你世界";
        Assertions.assertTrue(str.replace("我", "I").equals(str.replaceFirst("我", "I")));
        Assertions.assertTrue(str.replace("我", "I").equals(str.replaceAll("[我]", "I")));
        String nStr1 = str.replaceAll("[我]", "I").replaceAll("[爱]", "LOVE");
        System.out.println(nStr1);
        Assertions.assertTrue(str.replace("我", "I").replace("爱", "LOVE").equals(nStr1));

        String str2 = "abcde";
        Pattern pattern = Pattern.compile("[abcedfghijk]");
        Matcher matcher = pattern.matcher(str2);
        String str3 = matcher.replaceAll("").trim();
        Assertions.assertTrue(StringUtils.isBlank(str3));
    }

    @Test
    public void testSplit() {
        String str1 = "www.qq.com";
        String[] nStr1 = str1.split("\\.");
        for (String ch : nStr1) {
            System.out.println(ch);
        }
        System.out.println(Arrays.toString(nStr1));

        StringJoiner strJoiner = new StringJoiner(".");
        for (String ch : nStr1) {
            strJoiner.add(ch);
        }
        Assertions.assertTrue(str1.equals(strJoiner.toString()));

        Assertions.assertTrue(Arrays.stream(nStr1).collect(Collectors.joining(".")).equals(str1));
        Arrays.stream("a.b.c".split("\\.")).map(String::toUpperCase).collect(Collectors.joining(".")).equals("a.b.c");
    }

    @Test
    public void testTime() throws InterruptedException {
        Long startTime = System.currentTimeMillis();
        Thread.sleep(100L);

        double e = Math.E;
        System.out.format(Locale.CHINA, "%f%n", e);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = String.format("Current Datetime is %s", simpleDateFormat.format(new Date()));
        System.out.println(currentDate);

        Long endTime = System.currentTimeMillis();
        Assertions.assertTrue(endTime - startTime >= 100);
    }

    @Test
    public void testString() {
        String str = "This is String , split by StringTokenizer, created by runoob";
        StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
        while (stringTokenizer.hasMoreElements()) {
//            System.out.println("token:" + stringTokenizer.nextToken());
            System.out.println("element:" + stringTokenizer.nextElement());
        }
    }

    @Test
    public void testArray() {
        int array11[] = new int[]{1, 2, 3, 4, 9, 11, 8, 33, 22, 13};
        Arrays.sort(array11);

        Integer[] array1 = new Integer[]{1, 2, 3, 4, 9, 11, 8, 33, 22, 13};
        Arrays.sort(array1);
        System.out.println(Arrays.toString(array1));
        int index = Arrays.binarySearch(array1, 11);
        System.out.println(index);

        for (Integer item : array1) {
            System.out.println(item);
        }
        for (int i = 0; i < array1.length; i++) {
            System.out.format("index:%d, value:%d%n", i, array1[i]);
        }

        Collections.reverse(Arrays.asList(array1));
        System.out.println(Arrays.asList(array1));
    }

    @Test
    public void testLinkedList() {
        LinkedList<Integer> linkedList = new LinkedList<>();
        linkedList.add(11);
        linkedList.add(12);
        linkedList.add(14);
        linkedList.add(19);
        linkedList.add(13);
        linkedList.addFirst(10);
        linkedList.addLast(20);
        Assertions.assertTrue(linkedList.getFirst().equals(10));
        linkedList.subList(2, 3).clear();
        Assertions.assertTrue(linkedList.indexOf(10) == 0);
        Assertions.assertTrue(linkedList.lastIndexOf(10) == 0);
        Assertions.assertTrue(linkedList.get(1).equals(11));
        Collections.sort(linkedList, (a, b) -> b.compareTo(a));

        for (Integer item : linkedList) {
            System.out.println(item);
        }

        Iterator<Integer> iterator = linkedList.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        for (int i = 0; i < linkedList.size(); i++) {
            System.out.println(linkedList.get(i));
        }
    }

    private int sumvarargs(int... args) {
        int sum = 0;
        for (int i = 0; i < args.length; i++) {
            sum += args[i];
        }
        return sum;
    }

    @Test
    public void testVarargs() {
        int sum = sumvarargs(new int[]{1, 2, 3, 4});
        Assertions.assertTrue(sum == (1 + 2 + 3 + 4));
        verArgs(new int[]{11, 22, 33, 45});
        verArgs(new String[]{"aa", "bb"});
        verArgs(new boolean[]{true, false});
        verArgs("aabb", new int[]{11, 22, 33, 45});
    }

    private void verArgs(int... args) {
        for (int arg : args) {
            System.out.println(arg);
        }
    }

    private void verArgs(boolean... args) {
        for (boolean arg : args) {
            System.out.println(arg);
        }
    }

    private void verArgs(String... args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }

    private void verArgs(String a, int... args) {
        System.out.println(a);
        for (int arg : args) {
            System.out.println(arg);
        }
    }

    @Test
    public void testQueue() {
        Queue<String> queue = new LinkedList<>();
        queue.offer("A");
        queue.add("B");
        queue.add("C");
        queue.add("D");
        //todo
//        queue.stream().map(String::toLowerCase);
        System.out.println(queue);
        for (String q : queue) {
            System.out.println(q);
        }
//        System.out.println("queue poll:" + queue.poll());
//        System.out.println("queue element:" + queue.element());
//        System.out.println("queue peek:" + queue.peek());
        Assertions.assertTrue(queue.poll().equals("A"));
        Assertions.assertTrue(queue.element().equals(queue.peek()));
        for (String q : queue) {
            System.out.println(q);
        }
    }

    @Test
    public void testVector() {
        Vector v = new Vector();
        v.add(new Integer(11));
        v.add(new Integer(21));
        v.add(new Integer(22));
        v.add(new Integer(32));
        v.set(0, 32);
        Collections.swap(v, 1, 2);
        System.out.println(v);
        Assertions.assertTrue(Collections.max(v).equals(32));
    }

    @Test
    public void testLinked() {
        LinkedList linkedList = new LinkedList();
        linkedList.add(111);
        linkedList.add(112);
        linkedList.add(113);
        linkedList.set(1, 222);
        Collections.swap(linkedList, 1, 2);
        Assertions.assertTrue(linkedList.get(1).equals(113));
        Assertions.assertTrue(linkedList.get(2).equals(222));
    }

    @Test
    public void testList() {
        String[] cities = {"BeiJing", "HongKong", "ShangHai","TianJin"};

        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < cities.length; i++) {
            arrayList.add(cities[i]);
        }
        Assertions.assertTrue(arrayList.size() == cities.length);
        Assertions.assertTrue(Collections.max(arrayList, String.CASE_INSENSITIVE_ORDER).equals("TianJin"));
        //反转
        Collections.reverse(arrayList);
        System.out.println(arrayList);
        //打乱
        Collections.shuffle(arrayList);
        System.out.println(arrayList);
        if (!arrayList.isEmpty()){
            Assertions.assertTrue(arrayList.size() == cities.length);
            Assertions.assertTrue(arrayList.toArray().length == cities.length);
            //删除
            arrayList.remove(0);
            Assertions.assertTrue(arrayList.size() < cities.length);
        }

        //设置只读
        List list1 = Collections.unmodifiableList(arrayList);
        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () -> {
            list1.add("ShenZhen");
        });

        System.out.println("before rotate:"+arrayList);
        Collections.rotate(arrayList, 2);
        System.out.println("after rotate:"+arrayList);
    }

    @Test
    public void testSet() {
        String[] cities = {"BeiJing", "HongKong", "ShangHai"};

        //treeset
        TreeSet<String> treeSet = new TreeSet<>();
        for (int i = 0; i < cities.length; i++) {
            treeSet.add(cities[i]);
        }
        Assertions.assertTrue(Collections.max(treeSet, String.CASE_INSENSITIVE_ORDER).equals("ShangHai"));
//        System.out.println(Collections.min(treeSet,String.CASE_INSENSITIVE_ORDER));
        if (!treeSet.isEmpty()) {
            Assertions.assertTrue(treeSet.size() == cities.length);
        }

        Assertions.assertTrue(Collections.min(treeSet).equals("BeiJing"));
        for (String item : treeSet) {
            System.out.println(item);
        }

        //hashset
        HashSet<String> hashSet = new HashSet<>();
        for (int i = 0; i < cities.length; i++) {
            hashSet.add(cities[i]);
        }
        Assertions.assertTrue(Collections.max(hashSet).equals("ShangHai"));
        Assertions.assertTrue(Collections.min(hashSet, String.CASE_INSENSITIVE_ORDER).equals("BeiJing"));
        for (String item : hashSet) {
            System.out.println(item);
        }
        Iterator<String> iter = hashSet.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    @Test
    public void testCollectionRotate(){
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);

        System.out.println("原始列表: " + numbers);

        Collections.rotate(numbers, 4);

        System.out.println("旋转后列表: " + numbers);
    }

    public void testCollection() {
        List lnkLst = new LinkedList();
        lnkLst.add("element1");
        lnkLst.add("element2");
        lnkLst.add("element3");
        lnkLst.add("element4");
        displayAll(lnkLst);
        List aryLst = new ArrayList();
        aryLst.add("x");
        aryLst.add("y");
        aryLst.add("z");
        aryLst.add("w");
        displayAll(aryLst);
        Set hashSet = new HashSet();
        hashSet.add("set1");
        hashSet.add("set2");
        hashSet.add("set3");
        hashSet.add("set4");
        displayAll(hashSet);
        SortedSet treeSet = new TreeSet();
        treeSet.add("1");
        treeSet.add("2");
        treeSet.add("3");
        treeSet.add("4");
        displayAll(treeSet);
        LinkedHashSet lnkHashset = new LinkedHashSet();
        lnkHashset.add("one");
        lnkHashset.add("two");
        lnkHashset.add("three");
        lnkHashset.add("four");
        displayAll(lnkHashset);
        Map map1 = new HashMap();
        map1.put("key1", "J");
        map1.put("key2", "K");
        map1.put("key3", "L");
        map1.put("key4", "M");
        displayAll(map1.keySet());
        displayAll(map1.values());
        SortedMap map2 = new TreeMap();
        map2.put("key1", "JJ");
        map2.put("key2", "KK");
        map2.put("key3", "LL");
        map2.put("key4", "MM");
        displayAll(map2.keySet());
        displayAll(map2.values());
        LinkedHashMap map3 = new LinkedHashMap();
        map3.put("key1", "JJJ");
        map3.put("key2", "KKK");
        map3.put("key3", "LLL");
        map3.put("key4", "MMM");
        displayAll(map3.keySet());
        displayAll(map3.values());
    }

    private void displayAll(Collection col) {
        Iterator itr = col.iterator();
        while (itr.hasNext()) {
            String str = (String) itr.next();
            System.out.print(str + " ");
        }
        System.out.println();
    }

    @Test
    public void testMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.forEach((key, value) -> {
            System.out.println(key + ":" + value);
        });

        for (String key : map.keySet()) {
            System.out.println(key + ":" + map.get(key));
        }

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
