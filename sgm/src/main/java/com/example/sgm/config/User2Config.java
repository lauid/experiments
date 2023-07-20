package com.example.sgm.config;

import com.example.sgm.condition.ClassCondition;
import com.example.sgm.condition.ConditionOnClass;
import com.example.sgm.entity.User2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class User2Config {
    @Bean
//    @Conditional(ClassCondition.class)
    @ConditionOnClass("redis.clients.jedis.Jedis")
    public User2 user2(){
        return new User2();
    }

    @Bean
    @ConditionalOnProperty(name = "key",havingValue = "lauid")
    public User2 userx(){
        return new User2();
    }
}
