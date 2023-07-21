package com.example.sgm.controller;

import com.example.sgm.common.Result;
import com.example.sgm.common.ResultGenerator;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @RequestMapping(value = "/api//users/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Result<SysUserMapper> getOne(@PathVariable("id") Integer id) {
        if (id == null || id < 1) {
            return ResultGenerator.genFailResult("缺少id参数");
        }

        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null) {
            return ResultGenerator.genFailResult("无此数据");
        }

        return ResultGenerator.genSuccessResult(sysUser);
    }
}
