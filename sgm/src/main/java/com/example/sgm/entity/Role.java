package com.example.sgm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.stereotype.Repository;

@Data
public class Role {
    private Long id;
    @TableField("role_name")
    private String roleName;
    @TableField("role_describe")
    private String roleDescribe;
}
