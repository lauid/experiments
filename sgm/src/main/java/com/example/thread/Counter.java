package com.example.thread;

public class Counter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized void decrement() {
        count--;
    }

    public synchronized int getCount() {
        return count;
    }

    public static void testMain() throws InterruptedException {
        Counter counter = new Counter();

        Runnable incrementTask = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        Runnable decrementTask = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.decrement();
            }
        };

        Thread t1 = new Thread(incrementTask);
        Thread t2 = new Thread(decrementTask);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Count: " + counter.getCount());
    }
}
