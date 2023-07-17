package com.example.sgm.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

@Data
@Accessors(chain = true)
public class User2 {
    private Long id;
    @TableField(condition = SqlCondition.LIKE, jdbcType = JdbcType.VARCHAR)
    private String name;
    private Integer age;
}
