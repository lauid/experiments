package com.lauid.tz;

public class Test1 {
    public static void main(String[] args) {
        MyRunnable myRunnable = new MyRunnable();
        for (int i = 0; i < 100; i++) {
            new Thread(myRunnable).start();
        }
    }
}
