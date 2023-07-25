package com.example.el;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@ComponentScan("com.example.el")
@PropertySource(value = "t.properties", encoding = "UTF-8")
public class ELConfig {
    @Value("I LOVE YOU.")
    private String normal;
    @Value("#{systemProperties['os.name']}")
    private String osName;
    @Value("#{systemEnvironment['os.arch']}")
    private String osArch;

    @Value("#{T(java.lang.Math).random()*100}")
    private double randomNumber;

    @Value("#{demoService.author}")
    private String author;

    @Value("t.txt")
    private Resource testFile;

    @Value("https://yesno.wtf/api")
    private Resource testUrl;

    @Value("${sang.username}")
    private String su;
    @Value("${sang.password}")
    private String sp;
    @Value("${sang.nickname}")
    private String sn;

    @Autowired
    private Environment environment;

    public void output() {
        try {
            System.out.println(normal);
            System.out.println(osName);
            System.out.println(osArch);
            System.out.println(randomNumber);
            System.out.println(author);
            System.out.println(IOUtils.toString(testUrl.getInputStream(), "UTF-8"));
            System.out.println("testUrl.getURL" + testUrl.getURL());
            System.out.println("testUrl.getURI" + testUrl.getURI());
            System.out.println(su);
            System.out.println(sp);
            System.out.println(sn);
            System.out.println(environment.getProperty("sang.username"));
            System.out.println(IOUtils.toString(testFile.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
