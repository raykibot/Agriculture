package com.luo.share.model.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private Long userId;
    private String realName;
    private String idCard;

    // 地址信息
    private String province;
    private String city;
    private String district;
    private String detailAddress;
}