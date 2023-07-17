package com.example.sgm;

import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import com.example.sgm.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class IdGeneratorTest {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private UserService userService;

    @Test
    public void test() {
        SysUser sysUser = new SysUser();
        sysUser.setName("Tim");
        sysUser.setAge(22);
        sysUserMapper.insert(sysUser);
        Assertions.assertEquals(Long.valueOf(100L), sysUser.getId());

        testBatch();
    }

    private void testBatch() {
        List<SysUser> sysUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SysUser sysUser = new SysUser();
            sysUser.setName("Tim" + i);
            sysUser.setAge(18 + i);
            sysUsers.add(sysUser);
        }
        boolean result = userService.saveBatch(sysUsers);
        Assertions.assertEquals(true, result);
    }
}
