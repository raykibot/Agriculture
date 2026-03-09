package com.luo.share.controller;

import com.luo.share.common.api.Result;
import com.luo.share.model.dto.UserLoginDTO;
import com.luo.share.model.dto.UserRegisterDTO;
import com.luo.share.model.vo.LoginVO;
import com.luo.share.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
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

}