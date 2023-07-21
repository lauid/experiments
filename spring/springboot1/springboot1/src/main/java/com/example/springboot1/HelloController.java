package com.example.springboot1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Value("${person.name}")
    private String personName;
    @Value("${name}")
    private String name;
    @Value("${person.age}")
    private Integer age;

    @Autowired
    private Environment env;

    @Autowired
    private Person person;

    @RequestMapping("/hello")
    public String Hello(){
        System.out.println(env.getProperty("person.name"));
        System.out.println(env.getProperty("name"));
        String[] addresses = person.getAddress();
        for (String s : addresses){
            System.out.println(s);
        }
        return "hello world."+this.name + ";"+this.personName +";"+this.age.toString();
    }
}
