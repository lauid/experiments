package com.example.sgm.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Conditional(ClassCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionOnClass {
    String[] value();
}
