package com.example.springboot1;

import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Springboot1Application.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void TestAdd() {
        userService.add();
        Assertions.assertTrue(true);
    }
}
