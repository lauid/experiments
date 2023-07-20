package com.example.sgm.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Transactional
    public boolean insertTest() {
        SysUser sysUser = new SysUser();
        sysUser.setName("dog");
        sysUser.setAge(11);
        userMapper.insert(sysUser);
        userMapper.update(sysUser, Wrappers.<SysUser>lambdaUpdate().eq(SysUser::getId, 10));

        return true;
    }

    public void stateTrans() {
        // 非事务逻辑代码

        // 开始事务
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 事务逻辑代码
            SysUser sysUser = new SysUser();
            sysUser.setName("dog");
            sysUser.setAge(11);
            userMapper.insert(sysUser);
            userMapper.insert(sysUser);

            // 提交事务
            transactionManager.commit(status);
        } catch (Exception e) {
            // 回滚事务
            log.warn(e.getMessage());
            System.out.println("rollback.");
            transactionManager.rollback(status);
        }
    }

}
