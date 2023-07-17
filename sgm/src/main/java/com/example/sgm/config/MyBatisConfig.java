package com.example.sgm.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.sgm.mapper")
public class MyBatisConfig {
}
