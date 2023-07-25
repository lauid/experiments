package com.example.el;

import com.example.sgm.SgmApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootTest(classes = SgmApplication.class)
public class ELTest {
    @Test
    public void elTest() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        ELConfig bean = context.getBean(ELConfig.class);
        bean.output();
        context.close();
    }
}
