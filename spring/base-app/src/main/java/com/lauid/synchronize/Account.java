package com.lauid.synchronize;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {

    private String cardId;
    private double money;

    private final Lock lk = new ReentrantLock();

    public Account(String lauid, int i) {
        this.cardId = lauid;
        this.money = i;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    //同步方法,锁 this
    public void drawMoney(double money) {
        String threadName = Thread.currentThread().getName();

        lk.lock();
        try {
            if (this.money >= money) {
                System.out.println(threadName + " 钱足够:" + this.money);
                this.money -= money;
                System.out.println(threadName + " 扣除之后:" + this.money);
            } else {
                System.out.println(threadName + " 钱不够:" + this.money);
            }
        } catch (Exception e) {
        } finally {
            lk.unlock();
        }
    }

    //同步方法,锁 this
    synchronized public void drawMoney4(double money) {
        String threadName = Thread.currentThread().getName();
        if (this.money >= money) {
            System.out.println(threadName + " 钱足够:" + this.money);
            this.money -= money;
            System.out.println(threadName + " 扣除之后:" + this.money);
        } else {
            System.out.println(threadName + " 钱不够:" + this.money);
        }
    }

    //方法2，同步方法,锁 Account.class
    synchronized public static void drawMoney3(double money) {
    }

    public void drawMoney2(double money) {
        String threadName = Thread.currentThread().getName();
        //方法1，同步代码块
        synchronized (this) {
            if (this.money >= money) {
                System.out.println(threadName + " 钱足够:" + this.money);
                this.money -= money;
                System.out.println(threadName + " 扣除之后:" + this.money);
            } else {
                System.out.println(threadName + " 钱不够:" + this.money);
            }
        }
    }

    public void drawMoney1(double money) {
        String threadName = Thread.currentThread().getName();
        synchronized ("card") {
            if (this.money >= money) {
                System.out.println(threadName + " 钱足够:" + this.money);
                this.money -= money;
                System.out.println(threadName + " 扣除之后:" + this.money);
            } else {
                System.out.println(threadName + " 钱不够:" + this.money);
            }
        }
    }
}
