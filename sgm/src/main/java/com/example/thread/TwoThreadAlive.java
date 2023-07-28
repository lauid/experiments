package com.example.thread;

public class TwoThreadAlive extends Thread {
    public void run() {
        for (int i = 0; i < 5; i++) {
            printMsg();
        }
    }

    public void printMsg() {
        Thread thread = Thread.currentThread();
        String name = thread.getName();
        System.out.println("name=" + name);
    }

    public static void doHandle() {
        TwoThreadAlive twoThreadAlive = new TwoThreadAlive();
        twoThreadAlive.setName("ThreadName");
        System.out.println("before start(), tt.isAlive()=" + twoThreadAlive.isAlive());
        twoThreadAlive.start();
        twoThreadAlive.run();
        System.out.println("after start(), tt.isAlive()=" + twoThreadAlive.isAlive());
    }
}
