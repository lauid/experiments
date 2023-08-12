package com.lauid.communication;

public class ThreadTest {
    public static void main(String[] args) {
        Desk desk = new Desk();

        new Thread(() -> {
            while (true){
                desk.put();
            }
        }, "厨师1").start();

        new Thread(() -> {
            while (true){
                desk.put();
            }

        }, "厨师2").start();

        new Thread(() -> {
            while (true){
                desk.put();
            }
        }, "厨师3").start();


        new Thread(() -> {
            while (true){
                desk.get();
            }
        }, "吃货1").start();
        new Thread(() -> {
            while (true){
                desk.get();
            }
        }, "吃货2").start();

        System.out.println("---------");
    }
}