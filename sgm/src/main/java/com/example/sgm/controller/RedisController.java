package com.example.sgm.controller;

import com.example.sgm.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/hello1")
    public String hello(){
        return "hello";
    }

    @GetMapping("/addStringToRedis")
    @ResponseBody
    public Boolean addStringToRedis(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
        return true;
    }

    @GetMapping("/getStringToRedis")
    @ResponseBody
    public String getStringToRedis(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @GetMapping("/addUserToRedis")
    @ResponseBody
    public Boolean addUserToRedis(String name, Integer age) {
        SysUser user = new SysUser();
        user.setName(name);
        user.setAge(age);
        redisTemplate.opsForValue().set(name, user);
        return true;
    }

    @GetMapping("/getUserFromRedis")
    @ResponseBody
    public SysUser getUserFromRedis(String name) {
        return (SysUser) redisTemplate.opsForValue().get(name);
    }

    @GetMapping("/deleteUserFromRedis")
    @ResponseBody
    public Boolean deleteUserFromRedis(String name) {
        return redisTemplate.delete(name);
    }
}
