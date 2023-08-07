package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class Person {
    @NotBlank(message = "userId不能为空", groups = {GroupA.class})
    /**用户id*/
    private Integer userId;

    @NotBlank(message = "用户名不能为空", groups = {GroupA.class})
    /**用户id*/
    private String name;

    @Length(min = 30, max = 40, message = "必须在[30,40]", groups = {GroupB.class})
    @Length(min = 20, max = 30, message = "必须在[20,30]", groups = {GroupA.class})
    /**用户名*/
    private int age;
}
