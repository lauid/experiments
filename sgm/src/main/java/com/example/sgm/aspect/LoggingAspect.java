package com.example.sgm.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;


@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.sgm.service.*.*(..))")
    public void beforeAdvice(JoinPoint joinPoint) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String df = dateFormat.format(new Date());
        System.out.println(df + " 执行前置通知.");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        System.out.println(df + " 方法规则式拦截："+method.getName());
    }

    @Pointcut("@annotation(com.example.sgm.aspect.Action)")
    public void annotationPointCut() {
        System.out.println("method annotationPointCut");
    }

    //after表示先执行方法，后拦截，before表示先拦截，后执行方法
//    @Before("annotationPointCut()")
    @After("annotationPointCut()")
    public void after(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Action action = method.getAnnotation(Action.class);
        System.out.println("注解式拦截:"+action.name());
    }
}
