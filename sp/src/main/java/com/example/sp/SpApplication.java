package com.example.sp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.sp.mapper")
public class SpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpApplication.class, args);
	}

}
