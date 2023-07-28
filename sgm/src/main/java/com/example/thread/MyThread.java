package com.example.thread;

public class MyThread extends Thread {
    public boolean waiting = true;
    public boolean ready = false;

    public MyThread() {

    }

    public synchronized void startWait() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("wait() interrupted");
            }
        }
    }

    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " starting...");
        while (waiting)
            System.out.println("waiting:" + waiting);
        System.out.println("waiting");
        startWait();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
            System.out.println(threadName + " interrupted.");
        }
        System.out.println(threadName + " terminating.");
    }

    public synchronized void notice() {
        ready = true;
        notify();
    }
}
