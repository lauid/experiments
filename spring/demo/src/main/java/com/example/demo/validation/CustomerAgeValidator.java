package com.example.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.*;

@Min(value = 18, message = "年龄最小不能小于18")
@Max(value = 120, message = "年龄最大不能超过120")
@Constraint(validatedBy = {})
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomerAgeValidator {
    String message() default "年龄必须大于18且小于120";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
