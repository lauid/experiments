package com.example.sp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@TableName("product")
@Data
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private BigDecimal price;
    // 其他字段省略

    // Getter和Setter方法省略

    // 可选：重写toString方法，方便打印实体对象信息
}

