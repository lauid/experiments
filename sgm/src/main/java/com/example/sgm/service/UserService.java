package com.example.sgm.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {
}
