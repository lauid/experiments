package com.example.async;

import com.example.sgm.SgmApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootTest(classes = SgmApplication.class)
public class AsyncTest {
    @Test
    public void testAsync() throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TaskExecutorConfig.class);
        AsyncTaskService bean = context.getBean(AsyncTaskService.class);
        bean.executeAsyncTask2(111);
        for (int i = 0; i < 10; i++) {
            bean.executeAsyncTask1(i);
        }
        Thread.sleep(1000 * 100);
        context.close();
    }
}
