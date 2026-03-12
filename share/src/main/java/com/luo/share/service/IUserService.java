package com.luo.share.service;

import com.luo.share.model.dto.AddressAddDTO;
import com.luo.share.model.dto.UserProfileUpdateDTO;
import com.luo.share.model.entity.UserAddress;
import com.luo.share.model.vo.LoginVO;
import com.luo.share.model.vo.UserProfileVO;

import java.util.List;

public interface IUserService {
    void register(String phone, String username, String password);

    LoginVO login(String phone, String password);

    void logout(String token);

    UserProfileVO getUserProfile(Long userId);

    void updateProfile(UserProfileUpdateDTO dto);

    List<UserAddress> getAddressList(Long userId);

    void addAddress(AddressAddDTO dto);

    void setDefaultAddress(Long userId, Long addressId);

    void deleteAddress(Long userId, Long addressId);
}
