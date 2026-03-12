package com.luo.share.controller;

import com.luo.share.common.api.Result;
import com.luo.share.model.dto.AddressAddDTO;
import com.luo.share.model.dto.UserLoginDTO;
import com.luo.share.model.dto.UserProfileUpdateDTO;
import com.luo.share.model.dto.UserRegisterDTO;
import com.luo.share.model.entity.UserAddress;
import com.luo.share.model.vo.LoginVO;
import com.luo.share.model.vo.UserProfileVO;
import com.luo.share.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto) {
        // 在生产环境中，这里通常会配合 @Valid 注解做参数基础校验（非空、长度等）
        userService.register(dto.getPhone(), dto.getUsername(), dto.getPassword());
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody UserLoginDTO dto) {
        LoginVO loginVO = userService.login(dto.getPhone(), dto.getPassword());
        return Result.success(loginVO, "登录成功");
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.success("您已退出登录"); // 如果没传token，说明本身就没登录，直接返回成功
        }

        // 通常前端传的 token 带有 "Bearer " 前缀，我们需要截取掉
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        userService.logout(token);
        return Result.success("退出登录成功");
    }


    /**
     * 获取当前登录用户的个人资料
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getUserProfile(@RequestParam Long userId) {
        UserProfileVO profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    /**
     * 修改/绑定个人资料
     */
    @PostMapping("/profile/update")
    public Result<String> updateProfile(@RequestBody UserProfileUpdateDTO dto) {
        userService.updateProfile(dto);

        log.info("更新个人信息");
        return Result.success("资料更新成功");
    }

    // ================= 地址管理相关接口 =================

    @GetMapping("/address/list")
    public Result<List<UserAddress>> getAddressList(@RequestParam Long userId) {
        return Result.success(userService.getAddressList(userId));
    }

    @PostMapping("/address/add")
    public Result<String> addAddress(@RequestBody AddressAddDTO dto) {
        userService.addAddress(dto);
        return Result.success("地址添加成功");
    }

    @PostMapping("/address/default")
    public Result<String> setDefaultAddress(@RequestParam Long userId, @RequestParam Long addressId) {
        userService.setDefaultAddress(userId, addressId);
        return Result.success("设置默认成功");
    }

    @PostMapping("/address/delete")
    public Result<String> deleteAddress(@RequestParam Long userId, @RequestParam Long addressId) {
        userService.deleteAddress(userId, addressId);
        return Result.success("删除成功");
    }

}