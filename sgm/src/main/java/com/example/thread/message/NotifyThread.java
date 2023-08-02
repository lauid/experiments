package com.example.thread.message;

public class NotifyThread implements Runnable {
    private Message message;

    public NotifyThread(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        synchronized (message) {
            String content = "Hello world!";
            message.setContent(content);
            System.out.println("通知线程收到消息：" + content);
        }
    }
}
