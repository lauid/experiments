package com.example.sgm;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import com.example.sgm.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UpdateWrapperTest {
    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * UPDATE user SET age=?, email=? WHERE (name = ?)
     */
    @Test
    public void test1() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(22);
        sysUser.setEmail("22@qq.com");
        //1
        int res = userMapper.update(sysUser, new UpdateWrapper<SysUser>()
                .eq("name", "Tom"));
        Assertions.assertEquals(1, res);
        //2
        res = userMapper.update(null, new UpdateWrapper<SysUser>()
                .set("age", 23).set("email", "23@qq.com").eq("name", "Tom"));
        Assertions.assertEquals(1, res);
    }

    /**
     * 使用lambda条件构造器
     */
    @Test
    public void testLambda() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(33);
        sysUser.setEmail("33@qq.com");

        //1
        int res = userMapper.update(sysUser, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getName, "Tom"));
        Assertions.assertEquals(1, res);

        //2
        res = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getAge, 33)
                .set(SysUser::getEmail, "33@qq.com")
                .eq(SysUser::getName, "Tom"));
        Assertions.assertEquals(1, res);

        List<SysUser> users = userMapper.selectList(null);
        Assertions.assertEquals(5, users.size());
        users.forEach(System.out::println);
    }

    @Test
    public void testInsert() {
        userService.insertTest();
        userService.stateTrans();
    }

}
