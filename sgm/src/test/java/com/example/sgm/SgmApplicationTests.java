package com.example.sgm;

import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SgmApplicationTests {

    @Autowired
    private SysUserMapper userMapper;

    @Test
    void testSelect() {
        System.out.println(("----- selectAll method test ------"));

        List<SysUser> users = userMapper.selectList(null);
        Assertions.assertEquals(5, users.size());
        users.forEach(System.out::println);
    }
}
