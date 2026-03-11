package com.luo.share.service;

import com.luo.share.model.dto.UserProfileUpdateDTO;
import com.luo.share.model.vo.LoginVO;
import com.luo.share.model.vo.UserProfileVO;

public interface IUserService {
    void register(String phone, String username, String password);

    LoginVO login(String phone, String password);

    void logout(String token);

    UserProfileVO getUserProfile(Long userId);

    void updateProfile(UserProfileUpdateDTO dto);
}
