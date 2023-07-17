package com.example.sgm.controller;

import com.example.sgm.entity.SysUser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RequestTestController {

    @RequestMapping(value = "/test1", method = RequestMethod.GET)
    public String test1(String info) {
        if (StringUtils.isEmpty(info)) {
            return "请输入info的值！";
        }
        return "你输入的内容是:" + info;
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public List<SysUser> test2() {
        List<SysUser> users = new ArrayList<>();
        SysUser user1 = new SysUser();
        user1.setId(1L);
        user1.setName("十一");

        SysUser user2 = new SysUser();
        user2.setId(2L);
        user2.setName("十二");

        SysUser user3 = new SysUser();
        user3.setId(3L);
        user3.setName("十三");

        users.add(user1);
        users.add(user2);
        users.add(user3);

        return users;
    }

}
