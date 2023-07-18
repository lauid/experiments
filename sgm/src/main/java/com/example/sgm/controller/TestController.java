package com.example.sgm.controller;

import com.example.sgm.entity.SysUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @RequestMapping("/test/messageconvert1")
    @ResponseBody
    public SysUser messageConvert1() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(11);
        sysUser.setName("cat");
        sysUser.setEmail("cat@qq.com");

        return sysUser;
    }

    @RequestMapping("/test/messageconvert2")
    @ResponseBody
    public SysUser messageConvert2(@RequestBody SysUser sysUser) {
        System.out.println(sysUser.toString());

        sysUser.setName("Name:" + sysUser.getName());

        return sysUser;
    }
}
