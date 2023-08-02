package com.example.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskService {
    @Async
    public void executeAsyncTask1(int i) {
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("异步任务1：Thread.currentThread().getName():" + i + Thread.currentThread().getName());
    }

    @Async
    public void executeAsyncTask2(int i) {
        System.out.println("异步任务2：Thread.currentThread().getName():" + i + Thread.currentThread().getName());
    }
}
