package com.example.thread;

public class DeadLockExample {
    private static Object resource1 = new Object();
    private static Object resource2 = new Object();

    public static void testMain() {
        Thread thread1 = new Thread(() -> {
            synchronized (resource1) {
                System.out.println("Thread1: holding resource 1");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Thread1: waiting for resource 2");

                synchronized (resource2) {
                    System.out.println("Thread1: holding resource 1 and resource 2");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (resource2) {
                System.out.println("Thread2: holding resource 2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread2: waiting for resource 1");
                synchronized (resource1) {
                    System.out.println("Thread2: holding resource 1 and resource 2");
                }
            }
        });

        thread1.start();
        thread2.start();
    }
}
