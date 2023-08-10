package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@SpringBootTest
public class QueueTest {
    @Test
    public void producerConsumerBlockQueueTest() throws InterruptedException {
        ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue<>(5);
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    arrayBlockingQueue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getId() + "producer put:" + i);
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Thread consumer = new Thread(() -> {
            try {
                int count = 0;
                while (true) {
                    int element = (int) arrayBlockingQueue.take();
                    System.out.println(Thread.currentThread().getId() + "consumer take:" + element);
                    ++count;
                    if (count == 10) {
                        break;
                    }
                }
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //启动线程
        producer.start();
        consumer.start();

        //等待结束
        producer.join();
        consumer.join();

        countDownLatch.await();

        producer.interrupt();
        consumer.interrupt();
    }

    @Test
    public void nonBlockQueueTest() {
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(3);
        System.out.println(arrayBlockingQueue.offer("A"));
        System.out.println(arrayBlockingQueue.offer("B"));
        System.out.println(arrayBlockingQueue.offer("C"));

        System.out.println(arrayBlockingQueue.poll());
        System.out.println(arrayBlockingQueue.poll());
        System.out.println(arrayBlockingQueue.poll());
        System.out.println(arrayBlockingQueue.poll());
    }

    private final Integer THREAD_RET = 2;
    private final long THREAD_SLEEP_TIME = 1000L;

    class MyCallable implements Callable<Integer> {
        private final Integer n;

        MyCallable(Integer n) {
            this.n = n;
        }

        @Override
        public Integer call() throws Exception {
            for (int i = 0; i < this.n; i++) {
                System.out.println(i);
                Thread.sleep(THREAD_SLEEP_TIME);
            }
            return THREAD_RET;
        }
    }

    @Test
    public void testThreadCallable() throws ExecutionException, InterruptedException {
        // 使用
        ExecutorService executor = Executors.newCachedThreadPool();
        MyCallable myCallable = new MyCallable(10);
        Future<Integer> result = executor.submit(myCallable);
        System.out.println(result.get());
        Assertions.assertEquals(THREAD_RET, result.get());
    }

    @Test
    public void testThreadCallable2() throws ExecutionException, InterruptedException, TimeoutException {
        // 使用
        ExecutorService executor = Executors.newCachedThreadPool();
        MyCallable myCallable = new MyCallable(5);
        Future<Integer> result = executor.submit(myCallable);
        System.out.println(result.get());
        Assertions.assertEquals(THREAD_RET, result.get(THREAD_SLEEP_TIME + 10, TimeUnit.MILLISECONDS));

        try {
            System.out.println(result.get(THREAD_SLEEP_TIME - 500, TimeUnit.MILLISECONDS));
        } catch (Throwable e) {
            Assertions.assertThrowsExactly(TimeoutException.class, (Executable) e);
        }
    }

    @Test
    public void test1() {
        Random random = new Random();
        boolean b = random.nextBoolean();
        Assertions.assertTrue(b || !b);
    }

    class MyThread1 extends Thread {
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
            }
        }
    }

    @Test
    public void testThread1() {
        MyThread1 myThread1 = new MyThread1();
        myThread1.start();
    }

    class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                System.out.println(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testThread2() {
        MyRunnable myRunnable = new MyRunnable();
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    @Test
    public void testThread21() {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(i);
            }
        });
        thread.start();
    }

    class MyCallable3 implements Callable<String> {
        Integer num;

        MyCallable3(Integer num) {
            this.num = num;
        }

        @Override
        public String call() throws Exception {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                this.num += i;
            }
            return this.num.toString();
        }
    }

    @Test
    public void testThread3() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MyCallable3 myCallable3 = new MyCallable3(5);
        Future<String> submit = executorService.submit(myCallable3);
        executorService.shutdown();

        Thread.sleep(2000L);

        System.out.println("异步线程执行任务结果：" + submit.get());
        System.out.println("异步线程执行任务是否完毕：" + submit.isDone());
        System.out.println("异步线程执行任务是否已取消：" + submit.isCancelled());
    }

    @Test
    public void testJSONOBJECT() {
        List<JSONObject> list = new ArrayList<>();
        JSONObject listObject1 = new JSONObject();
        listObject1.put("a", "A");
        JSONObject listObject2 = new JSONObject();
        listObject2.put("b", "B");
        JSONObject listObject3 = new JSONObject();
        listObject3.put("c", "C");
        list.add(listObject1);
        list.add(listObject2);
        list.add(listObject3);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 200);
        jsonObject.put("msg", "success");
        jsonObject.put("data", list);
        System.out.println(jsonObject.toJSONString());
    }
}