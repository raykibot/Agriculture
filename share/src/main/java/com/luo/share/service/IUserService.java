package com.luo.share.service;

import com.luo.share.model.vo.LoginVO;

public interface IUserService {
    void register(String phone, String username, String password);

    LoginVO login(String phone, String password);

    void logout(String token);
}
