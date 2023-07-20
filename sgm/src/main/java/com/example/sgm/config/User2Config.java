package com.example.sgm.config;

import com.example.sgm.condition.ClassCondition;
import com.example.sgm.entity.User2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class User2Config {
    @Bean
    @Conditional(ClassCondition.class)
    public User2 user2(){
        return new User2();
    }
}
