package com.train.trainticketserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String idnum;

    private String role;

}