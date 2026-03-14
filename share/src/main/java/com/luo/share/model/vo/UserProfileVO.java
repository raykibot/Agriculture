package com.luo.share.model.vo;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String phone;         // 脱敏后的手机号
    private String realName;      // 脱敏后的真实姓名 (如: *建国)
    private String idCard;        // 脱敏后的身份证号
    private String status;        // 认证状态: "已认证" 或 "未实名"
    private String regDate;       // 注册时间 (yyyy-MM-dd)
    private String defaultAddress;// 拼接好的默认地址
}