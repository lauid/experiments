package com.example.sgm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.sgm.entity.SysUser;
import com.example.sgm.mapper.SysUserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OptLockTest {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Order(0)
    @Test
    public void testUpdateByIdSucc() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(18);
        sysUser.setEmail("test@baomidou.com");
        sysUser.setName("optLocker");
        sysUser.setVersion(1);
        sysUserMapper.insert(sysUser);
        Long id = sysUser.getId();

        SysUser userUpdate = new SysUser();
        userUpdate.setId(id);
        userUpdate.setAge(19);
        userUpdate.setVersion(1);
        assertThat(sysUserMapper.updateById(userUpdate)).isEqualTo(1);
        assertThat(userUpdate.getVersion()).isEqualTo(2);
    }

    @Order(1)
    @Test
    public void testUpdateByIdSuccFromDb() {
        SysUser sysUser = sysUserMapper.selectById(1);
        int oldVersion = sysUser.getVersion();
        int i = sysUserMapper.updateById(sysUser);
        assertThat(i).isEqualTo(1);
        assertThat(oldVersion + 1).isEqualTo(sysUser.getVersion());
    }

    @Order(2)
    @Test
    public void testUpdateByIdFail() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(18);
        sysUser.setEmail("test@qq.com");
        sysUser.setName("test");
        sysUser.setVersion(1);
        sysUserMapper.insert(sysUser);
        Long id = sysUser.getId();


        SysUser sysUserUpdate = new SysUser();
        sysUserUpdate.setId(id);
        sysUserUpdate.setAge(19);
        sysUserUpdate.setName("test2");
        sysUserUpdate.setVersion(0);
        Assertions.assertEquals(0, sysUserMapper.updateById(sysUserUpdate));
    }

    @Order(3)
    @Test
    public void testUpdateByIdSuccWithNoVersion() {
        SysUser sysUser = new SysUser();
        sysUser.setAge(18);
        sysUser.setEmail("test@baomidou.com");
        sysUser.setName("optlocker");
        sysUser.setVersion(1);
        sysUserMapper.insert(sysUser);
        Long id = sysUser.getId();

        SysUser userUpdate = new SysUser();
        userUpdate.setId(id);
        userUpdate.setAge(19);
        userUpdate.setVersion(null);
        // Should update success as no version passed in
        Assertions.assertEquals(1, sysUserMapper.updateById(userUpdate));
        SysUser updated = sysUserMapper.selectById(id);
        // Version not changed
        Assertions.assertEquals(1, updated.getVersion().intValue());
        // Age updated
        Assertions.assertEquals(19, updated.getAge().intValue());
    }

    /**
     * 批量更新带乐观锁
     * <p>
     * update(et,ew) et:必须带上version的值才会触发乐观锁
     */
    @Order(4)
    @Test
    public void testUpdateByEntitySucc() {
        QueryWrapper<SysUser> ew = new QueryWrapper<>();
        ew.eq("version", 1);
        long count = sysUserMapper.selectCount(ew);

        SysUser entity = new SysUser();
        entity.setAge(28);
        entity.setVersion(1);

        // updated records should be same
        Assertions.assertEquals(count, sysUserMapper.update(entity, null));
        ew = new QueryWrapper<>();
        ew.eq("version", 1);
        // No records found with version=1
        Assertions.assertEquals(0, sysUserMapper.selectCount(ew).intValue());
        ew = new QueryWrapper<>();
        ew.eq("version", 2);
        // All records with version=1 should be updated to version=2
        Assertions.assertEquals(count, sysUserMapper.selectCount(ew).intValue());
    }
}
