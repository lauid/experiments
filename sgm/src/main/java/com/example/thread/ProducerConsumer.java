package com.example.thread;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 生产者-消费者问题
 */
public class ProducerConsumer {
    private Queue<Integer> buffer = new LinkedList<>();
    private final int CAPACITY = 5;

    public void produce() throws InterruptedException {
        int value = 0;
        while (true) {
            synchronized (this) {
                while (buffer.size() == CAPACITY) {
                    wait();
                }

                System.out.println("Producer produced:" + value);
                buffer.add(value++);
                notify();
                Thread.sleep(1000);
            }
        }
    }

    public void consume() throws InterruptedException {
        while (true) {
            synchronized (this) {
                while (buffer.isEmpty()) {
                    wait();
                }

                int consumeValue = buffer.poll();
                System.out.println("Consumer consumed:" + consumeValue);
                notify();
                Thread.sleep(1000);
            }
        }
    }


    public static void testMain(){
        ProducerConsumer pc = new ProducerConsumer();
        Thread producerThread = new Thread(()->{
            try {
                pc.produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread consumerThread = new Thread(()->{
            try {
                pc.consume();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        producerThread.start();
        System.out.println("producer start.");
        consumerThread.start();
        System.out.println("consumer start.");
    }

}
