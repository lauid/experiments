package com.example.sgm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringBootWebMvcConfigurer implements WebMvcConfigurer {
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/files/*") .addResourceLocations("file:C:\\share\\");
//        registry.addResourceHandler("/files/*") .addResourceLocations("file:/home/project/upload/");
    }
}
