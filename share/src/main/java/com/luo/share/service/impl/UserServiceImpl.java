package com.luo.share.service.impl;

import com.luo.share.common.utils.RedisUtil;
import com.luo.share.mapper.UserMapper;
import com.luo.share.model.entity.User;
import com.luo.share.model.vo.LoginVO;
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

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

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
}