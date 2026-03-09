package com.luo.share.model.dto;

import lombok.Data;


@Data
public class UserRegisterDTO {
    private String phone;
    private String username;
    private String password;
}