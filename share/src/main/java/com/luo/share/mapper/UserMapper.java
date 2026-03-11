package com.luo.share.mapper;

import com.luo.share.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 统计该手机号或用户名的数量
    int countByPhoneOrUsername(@Param("phone") String phone, @Param("username") String username);

    // 插入新用户
    void insertUser(User user);

    // 根据手机号查询用户
    User selectByPhone(@Param("phone") String phone);

    User selectById(Long userId);

    void updateById(User user);
}