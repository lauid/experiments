package com.example.thread.message;

public class Message {
    private String content;
    private boolean isSent = false;

    //获取消息
    public synchronized String getContent() {
        while (!isSent) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        isSent = false;
        notifyAll();
        return content;
    }

    public synchronized void setContent(String content) {
        while (isSent){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isSent = true;
        this.content = content;
        notifyAll();
    }
}
