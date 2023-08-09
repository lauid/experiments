package com.lauid.synchronize;

public class ThreadTest {
    public static void main(String[] args) {
        Account account = new Account("lauid", 100);
        DrawThread black = new DrawThread(account, "小黑");
        DrawThread white = new DrawThread(account, "小白");

        black.start();
        white.start();
    }
}
