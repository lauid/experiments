package com.example.sp.entity;

import com.example.sp.po.Attribute;
import lombok.Data;

import java.util.Date;

@Data
public class UserEntity {
    private Long id;
    private Date gmtCreate;
    private String createTime;
    private Long buyerId;
    private Long age;
    private String userNick1;
    private String userVerified;
    private Attribute attribute;
}
