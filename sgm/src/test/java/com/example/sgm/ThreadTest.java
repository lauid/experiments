package com.example.sgm;

import com.example.thread.*;
import com.example.thread.message.Message;
import com.example.thread.message.NotifyThread;
import com.example.thread.message.WaitThread;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ThreadTest {

    @Test
    public void testThread1() {
        TwoThreadAlive.doHandle();
    }

    @Test
    public void testThread2() throws InterruptedException {
        MyThread thread = new MyThread();
        thread.setName("MyThread #1");
        showThreadStatus(thread);

        thread.start();
        Thread.sleep(10);
        showThreadStatus(thread);

        thread.waiting = false;
        Thread.sleep(10);
        showThreadStatus(thread);

        thread.notice();
        Thread.sleep(10);
        showThreadStatus(thread);
        while (thread.isAlive())
            System.out.println("alive");
        showThreadStatus(thread);
    }

    private void showThreadStatus(Thread thread) {
        System.out.println("Name:" + thread.getName() + ", isAlive:" + thread.isAlive() + ", State:" + thread.getState());
    }

    @Test
    public void testProducerConsumer() throws InterruptedException {
        ProducerConsumer.testMain();
        Thread.sleep(10000);
    }

    @Test
    public void testDeadLockThread() throws InterruptedException {
        DeadLockExample.testMain();
        Thread.sleep(50000);
    }

    @Test
    public void testCounter() throws InterruptedException {
        Counter.testMain();
        Thread.sleep(50000);
    }

    @Test
    public void testWaitNotify() {
        Message message = new Message();

        //等待线程
        Thread waitThread = new Thread(new WaitThread(message));
        waitThread.start();

        //通知线程
        Thread notifyThread = new Thread(new NotifyThread(message));
        notifyThread.start();
    }
    @Test
    public void testWaitNotify2() throws InterruptedException {
        Message message = new Message();

        //等待线程
        Thread waitThread = new Thread(() -> {
            synchronized (message) {
                while (message.getContent() != "END"){
                    System.out.println("等待线程获取消息：" + message.getContent());
                }
            }
        });
        waitThread.start();

        //通知线程
        Thread notifyThread = new Thread(()->{
            synchronized (message){
                int count = 0 ;
                while (count++ < 10){
                    System.out.println(count);
                    message.setContent("Hello world...."+count);
                }
                message.setContent("END");
            }
        });
        notifyThread.start();

        waitThread.join();
        notifyThread.join();
    }
}
