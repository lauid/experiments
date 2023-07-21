package com.example.sgm.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.sgm.common.Result;
import com.example.sgm.common.ResultGenerator;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
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

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<SysUser>> queryAll() {
        List<SysUser> users = sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().gt(SysUser::getId, 0));
        return ResultGenerator.genSuccessResult(users);
    }

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> insert(@RequestBody SysUser sysUser) {
        if (StringUtils.isEmpty(sysUser.getName()) || sysUser.getAge() <= 0) {
            return ResultGenerator.genFailResult("not param.");
        }
        return ResultGenerator.genSuccessResult(sysUserMapper.insert(sysUser) > 0);
    }

    @RequestMapping(value = "/users", method = RequestMethod.PUT)
    @ResponseBody
    public Result<Boolean> update(@RequestBody SysUser tempUser) {
        if (tempUser.getId() == null || tempUser.getId() < 1 || StringUtils.isEmpty(tempUser.getName()) || tempUser.getAge() < 1) {
            return ResultGenerator.genFailResult("not param.");
        }

        SysUser sysUser = sysUserMapper.selectById(tempUser.getId());
        if (sysUser == null) {
            return ResultGenerator.genFailResult("param except");
        }
        sysUser.setName(tempUser.getName());
        sysUser.setAge(tempUser.getAge());
        return ResultGenerator.genSuccessResult(sysUserMapper.updateById(sysUser) > 0);
    }

    // 删除一条记录
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Result<Boolean> delete(@PathVariable("id") Integer id) {
        if (id == null || id < 1) {
            return ResultGenerator.genFailResult("缺少参数");
        }
        return ResultGenerator.genSuccessResult(sysUserMapper.deleteById(id) > 0);
    }
}
