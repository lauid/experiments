package com.example.sgm;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sgm.entity.SysUser;
import com.example.sgm.entity.User2;
import com.example.sgm.mapper.SysUserMapper;
import com.example.sgm.mapper.User2Mapper;
import org.h2.engine.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.notIn;

@SpringBootTest
public class CrudTest {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private User2Mapper user2Mapper;

    @Test
    public void aInsert() {
        SysUser sysUser = new SysUser();
        sysUser.setName("dog");
        sysUser.setAge(3);
        sysUser.setEmail("dog@qq.com");
        assertThat(sysUserMapper.insert(sysUser)).isGreaterThan(0);
        assertThat(sysUser.getId()).isNotNull();
    }

    @Test
    public void bDelete() {
        assertThat(sysUserMapper.deleteById(3L)).isGreaterThan(0);
        assertThat(sysUserMapper.delete(new QueryWrapper<SysUser>().lambda().eq(SysUser::getName, "Sandy"))).isGreaterThan(0);
    }

    @Test
    public void cUpdate() {
        assertThat(sysUserMapper.updateById(new SysUser().setId(1L).setEmail("ab@c.c"))).isGreaterThan(0);

        //
        assertThat(
                sysUserMapper.update(
                        new SysUser().setName("mp"),
                        Wrappers.<SysUser>lambdaUpdate().set(SysUser::getAge, 3).eq(SysUser::getId, 2)
                )
        ).isGreaterThan(0);
        SysUser sysUser = sysUserMapper.selectById(2);
        assertThat(sysUser.getAge()).isEqualTo(3);
        assertThat(sysUser.getName()).isEqualTo("mp");

        //
        sysUserMapper.update(
                null,
                Wrappers.<SysUser>lambdaUpdate().set(SysUser::getEmail, null).eq(SysUser::getId, 2)
        );
        assertThat(sysUserMapper.selectById(1).getEmail()).isEqualTo("ab@c.c");
        sysUser = sysUserMapper.selectById(2);
        assertThat(sysUser.getEmail()).isNull();
        assertThat(sysUser.getName()).isEqualTo("mp");


        //
        sysUserMapper.update(
                new SysUser().setEmail("cat@qq.com"),
                new QueryWrapper<SysUser>().lambda().eq(SysUser::getId, 2)
        );
        sysUser = sysUserMapper.selectById(2);
        assertThat(sysUser.getEmail()).isEqualTo("cat@qq.com");

        //
        sysUserMapper.update(
                new SysUser().setEmail("pig@qq.com"),
                Wrappers.<SysUser>lambdaUpdate().set(SysUser::getAge, null).eq(SysUser::getId, 2)
        );
        sysUser = sysUserMapper.selectById(2);
        assertThat(sysUser.getEmail()).isEqualTo("pig@qq.com");
        assertThat(sysUser.getAge()).isNull();
    }

    @Test
    public void dSelect() {
        sysUserMapper.insert(
                new SysUser().setId(10086L)
                        .setName("aa")
                        .setEmail("aa@qq.com")
                        .setAge(3)
        );

        assertThat(sysUserMapper.selectById(10086L).getEmail()).isEqualTo("aa@qq.com");

        SysUser sysUser = sysUserMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getId, 10086L));
        assertThat(sysUser.getName()).isEqualTo("aa");
        assertThat(sysUser.getAge()).isEqualTo(3);

        sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().select(SysUser::getId)).forEach(
                x -> {
                    assertThat(x.getId()).isNotNull();
//                    assertThat(x.getEmail()).isNotNull();
//                    assertThat(x.getAge()).isNotNull();
//                    assertThat(x.getName()).isNotNull();
                }
        );

        sysUserMapper.selectList(new QueryWrapper<SysUser>().select("id", "name")).forEach(
                x -> {
                    assertThat(x.getId()).isNotNull();
                    assertThat(x.getName()).isNotNull();
//                    assertThat(x.getEmail()).isNotNull();
//                    assertThat(x.getAge()).isNotNull();
                }
        );
    }

    @Test
    public void orderBy() {
        List<SysUser> users = sysUserMapper.selectList(Wrappers.<SysUser>query().orderByAsc("age"));
        assertThat(users).isNotEmpty();

        List<SysUser> users2 = sysUserMapper.selectList(Wrappers.<SysUser>query().orderByAsc("age", "name"));
        assertThat(users2).isNotEmpty();

        List<SysUser> users3 = sysUserMapper.selectList(Wrappers.<SysUser>query().orderByAsc("age").orderByDesc("name"));
        assertThat(users3).isNotEmpty();
    }

    @Test
    public void selectMaps(){
        List<Map<String,Object>> mapList = sysUserMapper.selectMaps(Wrappers.<SysUser>query().orderByAsc("age"));
        assertThat(mapList).isNotEmpty();
        assertThat(mapList.get(0)).isNotEmpty();
        System.out.println(mapList);
        System.out.println(mapList.get(0));
    }

    @Test
    public void selectMapsPage() {
        IPage<Map<String, Object>> page = sysUserMapper.selectMapsPage(new Page<>(1, 5), Wrappers.<SysUser>query().orderByAsc("age"));
        assertThat(page).isNotNull();
        assertThat(page.getRecords()).isNotEmpty();
        assertThat(page.getRecords().get(0)).isNotEmpty();
        System.out.println(page.getRecords().get(0));
    }

    @Test
    public void orderByLambda() {
        List<SysUser> users = sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().orderByAsc(SysUser::getAge));
        assertThat(users).isNotEmpty();
        //多字段排序
        List<SysUser> users2 = sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().orderByAsc(SysUser::getAge, SysUser::getName));
        assertThat(users2).isNotEmpty();
        //先按age升序排列，age相同再按name降序排列
        List<SysUser> users3 = sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().orderByAsc(SysUser::getAge).orderByDesc(SysUser::getName));
        assertThat(users3).isNotEmpty();
    }

    @Test
    public void testSelectMaxId() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.select("max(id) as id");
        SysUser sysUser= sysUserMapper.selectOne(wrapper);
        System.out.println("maxId=" + sysUser.getId());
        List<SysUser> users = sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery().orderByDesc(SysUser::getId));
        Assertions.assertEquals(sysUser.getId().longValue(), users.get(0).getId().longValue());
    }

    @Test
    public void testGroup() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.select("age, count(*)")
                .groupBy("age");
        List<Map<String, Object>> maplist = sysUserMapper.selectMaps(wrapper);
        for (Map<String, Object> mp : maplist) {
            System.out.println(mp);
        }

        /**
         * lambdaQueryWrapper groupBy orderBy
         */
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new QueryWrapper<SysUser>().lambda()
                .select(SysUser::getAge)
                .groupBy(SysUser::getAge)
                .orderByAsc(SysUser::getAge);
        for (SysUser sysUser : sysUserMapper.selectList(lambdaQueryWrapper)) {
            System.out.println(sysUser);
        }
    }

    @Test
    public void testTableFieldExistFalse() {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.select("age, count(age) as count")
                .groupBy("age");
        List<SysUser> list = sysUserMapper.selectList(wrapper);
        list.forEach(System.out::println);
        list.forEach(x -> {
            Assertions.assertNull(x.getId());
            Assertions.assertNotNull(x.getAge());
            Assertions.assertNotNull(x.getCount());
        });
        sysUserMapper.insert(
                new SysUser().setId(10088L)
                        .setName("miemie")
                        .setEmail("miemie@baomidou.com")
                        .setAge(3));
        SysUser miemie = sysUserMapper.selectById(10088L);
        Assertions.assertNotNull(miemie);
    }

    @Test
    public void testSqlCondition() {
        Assertions.assertEquals(user2Mapper.selectList(Wrappers.<User2>query()
                .setEntity(new User2().setName("n"))).size(), 2);
        Assertions.assertEquals(user2Mapper.selectList(Wrappers.<User2>query().like("name", "J")).size(), 2);
        Assertions.assertEquals(user2Mapper.selectList(Wrappers.<User2>query().gt("age", 18)
                .setEntity(new User2().setName("J"))).size(), 1);
    }
}
