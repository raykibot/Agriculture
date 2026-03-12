package com.luo.share.model.dto;

import lombok.Data;

@Data
public class AddressAddDTO {
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault; // 0-否, 1-是
}