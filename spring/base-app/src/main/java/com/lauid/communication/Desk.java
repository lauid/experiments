package com.lauid.communication;

import java.util.ArrayList;
import java.util.List;

public class Desk {
    private List<String> list = new ArrayList<>();

    synchronized public void put() {
        String name = Thread.currentThread().getName();
        if (list.isEmpty()) {
            list.add(name + "做的包子");
            System.out.println(name + "放进1个包子");

            //唤醒别人
            this.notifyAll();
            //自己等待
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //唤醒别人
            this.notifyAll();
            //自己等待
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void get() {
        String name = Thread.currentThread().getName();
        if (!list.isEmpty()) {
            System.out.println(name + "吃掉1个 " + list.get(0));
            list.clear();

            //唤醒别人
            this.notifyAll();
            //自己等待
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //唤醒别人
            this.notifyAll();
            //自己等待
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
