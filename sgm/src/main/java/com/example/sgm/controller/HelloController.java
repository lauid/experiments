package com.example.sgm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
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
}
