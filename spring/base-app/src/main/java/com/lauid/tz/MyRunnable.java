package com.lauid.tz;

import java.util.concurrent.atomic.AtomicInteger;

public class MyRunnable implements Runnable {
    private int count1;

    private AtomicInteger count = new AtomicInteger();

    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " : " + this.count.incrementAndGet());
        }
    }

    //悲观锁
    private void run1(){
        synchronized (this){
            for (int i = 0; i < 100; i++) {
                System.out.println(Thread.currentThread().getName() + " : " + ++this.count1);
            }
        }
    }
}
