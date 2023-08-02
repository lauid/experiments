package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;

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
    public void nonBlockQueueTest(){
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

    class Task implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            Thread.sleep(THREAD_SLEEP_TIME );
            return THREAD_RET;
        }
    }

    @Test
    public void testThreadCallable() throws ExecutionException, InterruptedException {
        // 使用
        ExecutorService executor = Executors.newCachedThreadPool();
        Task task = new Task();
        Future<Integer> result = executor.submit(task);
        System.out.println(result.get());
        Assertions.assertEquals(THREAD_RET, result.get());
    }

    @Test
    public void testThreadCallable2() throws ExecutionException, InterruptedException, TimeoutException {
        // 使用
        ExecutorService executor = Executors.newCachedThreadPool();
        Task task = new Task();
        Future<Integer> result = executor.submit(task);
        System.out.println(result.get());
        Assertions.assertEquals(THREAD_RET, result.get(THREAD_SLEEP_TIME + 10, TimeUnit.MILLISECONDS));

        try{
            System.out.println(result.get(THREAD_SLEEP_TIME - 500 , TimeUnit.MILLISECONDS));
        }catch (Throwable e){
            Assertions.assertThrowsExactly(TimeoutException.class, (Executable) e);
        }
    }
}
