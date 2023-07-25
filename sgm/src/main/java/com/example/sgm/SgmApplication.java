package com.example.sgm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
//import de.codecentric.boot.admin.server.config.EnableAdminServer;
//
//
//@EnableAdminServer
@EnableAspectJAutoProxy
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.example.sgm.mapper")
@EnableScheduling
public class SgmApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SgmApplication.class, args);

        Object u = context.getBean("userx");
        System.out.println(u);
    }

}
