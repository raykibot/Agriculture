package com.luo.share.model.vo;

import lombok.Data;

@Data
public class ContactVO {
    private Long id;          // 联系人用户ID
    private String name;      // 联系人真实用户名 (username)
    private String avatar;    // 联系人头像
    private String role;      // 角色 (可选，如没有可不传)
}