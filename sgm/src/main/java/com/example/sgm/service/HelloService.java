package com.example.sgm.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public String sayHello(String name) {
        return "hello service " + name;
    }
}
