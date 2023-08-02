package com.example.thread.message;

public class WaitThread implements Runnable {
    private Message message;

    public WaitThread(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        synchronized (message) {
            String receiveMessage = message.getContent();
            System.out.println("等待线程收到消息：" + receiveMessage);
        }
    }
}
