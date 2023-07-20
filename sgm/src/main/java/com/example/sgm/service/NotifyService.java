package com.example.sgm.service;


import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class NotifyService {
    public void sendMessage() {
        System.out.println(new Date() + "通知发送出去。");
        this.myMethod();
    }

    private void myMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        if (stackTraceElements.length >= 3) {
            StackTraceElement currentElement = stackTraceElements[2];

            String className = currentElement.getClassName();
            String methodName = currentElement.getMethodName();
            int lineNumber = currentElement.getLineNumber();

            System.out.println("当前行：" + lineNumber);
            System.out.println("当前方法：" + methodName);
            System.out.println("当前类：" + className);
        }
    }
}
