package com.example.springboot1;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void TestSet() {
        redisTemplate.boundValueOps("name").set("zhangsan");
    }

    @Test
    void TestGet() {
        Object nameValue = redisTemplate.boundValueOps("name").get();
        System.out.println(nameValue);
    }
}
