package com.example.sgm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.example.sgm.mapper")
public class SgmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgmApplication.class, args);
    }

}
