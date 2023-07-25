package com.example.springbootenable;

import com.example.springbootenableother.config.MyImportBeanDefinitionRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.Jedis;

/**
 * ComponentScan 当前引导类所在包以及子包
 * 1, @ComponentScan("com.example.springbootenableother.config")
 * 2, @Import 加载，这些类都会被spring创建，并放入ioc容器 @Import(UserConfig.class)
 * 3, @EnableUser
 * 4, @Import(User.class)
 * 5, @Import(UserConfig.class)
 * 6, @Import(MyImportSelector.class)
 * 7, @Import(MyImportBeanDefinitionRegistrar.class)
 */
@SpringBootApplication
@Import(MyImportBeanDefinitionRegistrar.class)
public class SpringbootEnableApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringbootEnableApplication.class, args);
//        Object user = context.getBean("user");
//        System.out.println(user);
//        Object role = context.getBean("role");
//        System.out.println(role);
//        Map<String,User> map = context.getBeansOfType(User.class);
//        System.out.println(map);
//        Object user = context.getBean(User.class);
//        System.out.println(user);
//        Object role = context.getBean(Role.class);
//        System.out.println(role);

        Jedis jedis = context.getBean(Jedis.class);
        System.out.println(jedis);
        jedis.set("aa","AA");
        jedis.set("bb","BB");
    }

    @Bean
    public Jedis jedis(){
        return new Jedis("localhost",6699);
    }
}
