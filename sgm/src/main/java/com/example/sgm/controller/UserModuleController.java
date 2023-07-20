package com.example.sgm.controller;

import com.example.sgm.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "用户模块接口")
@RestController
public class UserModuleController {
    static Map<Integer, SysUser> usersMap = Collections.synchronizedMap(new HashMap<Integer, SysUser>());

    static {
        SysUser user = new SysUser();
        user.setId(10L);
        user.setName("Cat");
        user.setEmail("Cat@qq.com");
        SysUser user2 = new SysUser();
        user2.setId(11L);
        user2.setName("Cat2");
        user2.setEmail("Cat2@qq.com");
        usersMap.put(1, user);
        usersMap.put(2, user2);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public List<SysUser> getUserList() {
        List<SysUser> users = new ArrayList<SysUser>(usersMap.values());
        return users;
    }

    @Operation(summary = "新增用户", description = "根据user对象新增用户")
    @PostMapping("/users")
    public String postUser(@RequestBody SysUser sysUser) {
        usersMap.put(Math.toIntExact(sysUser.getId()), sysUser);
        return "add success";
    }

    @Operation(summary = "获取用户信息", description = "根据用户id获取用户信息")
    @GetMapping("/users/{id}")
    public SysUser getUser(@PathVariable Integer id) {
        return usersMap.get(id);
    }

    @Operation(summary = "更新用户信息", description = "")
    @PutMapping("/users/{id}")
    public String putUser(@PathVariable Integer id, @RequestBody SysUser sysUser) {
        SysUser tempUser = usersMap.get(id);
        tempUser.setName(sysUser.getName());
        tempUser.setEmail(sysUser.getEmail());
        usersMap.put(id, tempUser);
        return "update success";
    }

    @Operation(summary = "删除用户", description = "根据用户id删除")
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Integer id) {
        usersMap.remove(id);
        return "remove success";
    }
}

