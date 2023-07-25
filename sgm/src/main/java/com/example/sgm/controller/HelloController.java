package com.example.sgm.controller;

import com.example.sgm.aspect.Action;
import com.example.sgm.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private HelloService helloService;

//    @Autowired
//    private ELConfig elConfig;

    @GetMapping("/hello")
    @ResponseBody
    @Action(name = "add-1")
    public String hello() {
        helloService.sayHello("abc");
        System.out.println("hello world.");
        return "hello world.";
    }

    @GetMapping("/hello2")
    @ResponseBody
    public String hello2(String name, Integer age) {
        return "hello2 world." + name + age;
    }

    @GetMapping("/log")
    @ResponseBody
    public String log() {
        logger.info("info log");
        logger.debug("debug log");
        logger.warn("warn log");

        return "log print";
    }

    @GetMapping("/el")
    @ResponseBody
    public String el() {
//        elConfig.output();
        return "el conifg output";
    }
}
