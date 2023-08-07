package com.example.demo.web;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.JSONResult;
import com.example.demo.model.User;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @RequestMapping(method = RequestMethod.POST, path = "/check")
    public String check(@RequestBody @Valid User user, BindingResult result) {
        String name = user.getName();
        if (result.hasErrors()) {
            for (ObjectError err : result.getAllErrors()) {
                System.out.println(err.getCode() + ":" + err.getDefaultMessage());
            }
        }

        return name;
    }

    @GetMapping(path = "/getUser", produces = "application/json")
    @ResponseBody
    public JSONResult getUser() {
        return getJsonResult();
    }

    @GetMapping(path = "/getUser2")
    public JSONResult getUser2() {
        Integer a = 1/0;
        return getJsonResult();
    }

    @NotNull
    private JSONResult getJsonResult() {
        User user = new User();
        user.setAddress("address");
        user.setEmail("abc@qq.com");
        user.setAge(20);
        user.setName("Name");
        JSONResult result = JSONResult.success(user);
        System.out.println(result);
        return result;
    }
}
