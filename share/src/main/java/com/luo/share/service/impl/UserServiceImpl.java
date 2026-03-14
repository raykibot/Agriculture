package com.luo.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luo.share.common.utils.RedisUtil;
import com.luo.share.mapper.UserAddressMapper;
import com.luo.share.mapper.UserMapper;
import com.luo.share.model.dto.AddressAddDTO;
import com.luo.share.model.dto.UserProfileUpdateDTO;
import com.luo.share.model.entity.User;
import com.luo.share.model.entity.UserAddress;
import com.luo.share.model.vo.LoginVO;
import com.luo.share.model.vo.UserProfileVO;
import com.luo.share.service.IUserService;
import com.luo.share.websocket.ChatWebSocketServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // 实例化 BCrypt 加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(String phone, String username, String password) {
        // 1. 调用手写 SQL 校验用户名或手机号是否已存在
        int count = userMapper.countByPhoneOrUsername(phone, username);
        if (count > 0) {
            throw new RuntimeException("手机号或用户名已存在");
        }

        // 2. 密码加密存储
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // BCrypt 强加密

        // 3. 调用手写 SQL 插入数据
        userMapper.insertUser(newUser);
    }

    @Override
    public LoginVO login(String phone, String password) {
        // 1. 根据手机号查询用户 (手写 SQL)
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // 2. 校验密码 (明文密码 vs 数据库里的密文)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }


        // 3. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();


        // 4. 将 Token 存入 Redis，设置 6 小时过期 (Key: login:token:{userId})
        String redisKey = "login:token:" + user.getId();
        log.info("token存储到redis中：{}", redisKey);
        redisUtil.setEx(redisKey, token, jwtExpiration);

        // 5. 封装 VO 返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        return loginVO;
    }

    @Override
    public void logout(String token) {
        try {
            // 1. 解析 Token 获取 userId
            // 注意：这里可能会抛出过期或签名异常，说明 token 已经无效了，直接捕获即可
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("userId", Long.class);

            // 2. 清除 Redis 中的登录状态
            if (userId != null) {
                String redisKey = "login:token:" + userId;
                redisUtil.delete(redisKey);
                log.info("用户 ID:{} 安全退出，已清除 Redis 缓存", userId);

                // 3. 强制断开该用户的 WebSocket 连接 (防僵尸连接)
                ChatWebSocketServer.forceDisconnect(userId);
            }
        } catch (Exception e) {
            // 如果 Token 已经过期或者格式不对，说明本身就属于无效状态，直接打印日志放行
            log.warn("退出登录时解析 Token 失败或已过期: {}", e.getMessage());
        }
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // 1. 查询用户基础信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());

        // 2. 手机号脱敏 (保留前3后4)
        String phone = user.getPhone();
        if (phone != null && phone.length() == 11) {
            vo.setPhone(phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        } else {
            vo.setPhone(phone);
        }

        // 3. 身份证与真实姓名脱敏 (实名认证判断)
        String idCard = user.getIdCard();
        String realName = user.getRealName();
        if (idCard != null && realName != null) {
            vo.setStatus("已认证");
            // 身份证保留前6后4
            vo.setIdCard(idCard.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2"));
            // 姓名只展示最后一个字，前面用星号代替 (如: 李建国 -> **国)
            String maskedName = realName.replaceAll(".", "*").substring(0, realName.length() - 1)
                    + realName.substring(realName.length() - 1);
            vo.setRealName(maskedName);
        } else {
            vo.setStatus("未实名");
            vo.setIdCard("未绑定");
            vo.setRealName("未绑定");
        }

        // 4. 格式化注册时间
        if (user.getCreateTime() != null) {
            vo.setRegDate(new SimpleDateFormat("yyyy-MM-dd").format(user.getCreateTime()));
        }

        // 5. 查询默认收货地址
        QueryWrapper<UserAddress> addressQuery = new QueryWrapper<>();
        addressQuery.eq("user_id", userId).eq("is_default", 1).eq("is_deleted",0).last("LIMIT 1");
        UserAddress defaultAddress = userAddressMapper.selectOne(addressQuery);

        if (defaultAddress != null) {
            String fullAddress = String.format("%s %s %s %s",
                    defaultAddress.getProvince(),
                    defaultAddress.getCity(),
                    defaultAddress.getDistrict(),
                    defaultAddress.getDetailAddress());
            vo.setDefaultAddress(fullAddress);
        } else {
            vo.setDefaultAddress("暂未设置默认地址");
        }

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProfile(UserProfileUpdateDTO dto) {
        // 1. 更新 users 表的实名信息
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (dto.getRealName() != null && !dto.getRealName().trim().isEmpty()) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getIdCard() != null && !dto.getIdCard().trim().isEmpty()) {
            user.setIdCard(dto.getIdCard());
        }
        userMapper.updateById(user);

        // 2. 更新或新增默认收货地址 user_addresses 表
        if (dto.getProvince() != null && !dto.getProvince().trim().isEmpty() &&
                dto.getDetailAddress() != null && !dto.getDetailAddress().trim().isEmpty()) {

            QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", dto.getUserId()).eq("is_default", 1).last("LIMIT 1");
            UserAddress address = userAddressMapper.selectOne(queryWrapper);

            if (address == null) {
                // 如果以前没有默认地址，则新建一条
                address = new UserAddress();
                address.setUserId(dto.getUserId());
                address.setIsDefault(1);
                address.setReceiverName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                address.setReceiverPhone(user.getPhone());
            }
            // 更新地址内容
            address.setProvince(dto.getProvince());
            address.setCity(dto.getCity());
            address.setDistrict(dto.getDistrict() != null ? dto.getDistrict() : "");
            address.setDetailAddress(dto.getDetailAddress());

            if (address.getId() == null) {
                userAddressMapper.insert(address);
            } else {
                userAddressMapper.updateById(address);
            }
        }
    }

    @Override
    public List<UserAddress> getAddressList(Long userId) {
        QueryWrapper<UserAddress> query = new QueryWrapper<>();
        // 按照是否默认降序，创建时间降序排列
        query.eq("user_id", userId).orderByDesc("is_default", "create_time").eq("is_deleted", 0);
        return userAddressMapper.selectList(query);
    }

    @Override
    public void addAddress(AddressAddDTO dto) {
        // 如果新增的地址被勾选为默认，需要先把之前的默认地址取消
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefaultAddress(dto.getUserId());
        }

        UserAddress address = new UserAddress();
        address.setUserId(dto.getUserId());
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);

        userAddressMapper.insert(address);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        // 1. 先把该用户所有的地址取消默认
        clearDefaultAddress(userId);

        // 2. 将指定的地址设为默认
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setIsDefault(1);
        userAddressMapper.updateById(address);
    }

    private void clearDefaultAddress(Long userId) {
        UserAddress updateObj = new UserAddress();
        updateObj.setIsDefault(0);
        QueryWrapper<UserAddress> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("is_default", 1);
        userAddressMapper.update(updateObj, query);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress updateObj = new UserAddress();
        updateObj.setIsDeleted(1);
        updateObj.setIsDefault(0);

        QueryWrapper<UserAddress> query = new QueryWrapper<>();
        query.eq("id", addressId).eq("user_id", userId);

        userAddressMapper.update(updateObj, query);
    }
}