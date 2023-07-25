package com.example.redisspringbootautoconfigure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass(Jedis.class)
public class RedisAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name="jedis")
    public Jedis jedis(RedisProperties redisProperties){
        System.out.println("redis autoconfiguration...");
        return new Jedis(redisProperties.getHost(),redisProperties.getPort());
    }
}