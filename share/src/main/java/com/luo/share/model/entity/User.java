package com.luo.share.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {


    private Long id;

    private String phone;

    private String password;

    private String username;

    private String avatar;

    private Integer role;

    private String realName;

    private String idCard;

    private Date createTime;

    private Date updateTime;

    private String isDeleted;

}
