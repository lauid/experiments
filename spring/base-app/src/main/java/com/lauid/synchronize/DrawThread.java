package com.lauid.synchronize;

public class DrawThread extends Thread{
    private Account acc;
    DrawThread(Account account, String name){
        super(name);
        acc= account;
    }

    @Override
    public void run() {
        acc.drawMoney(100);
    }
}
