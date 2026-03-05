package com.example.common.security.service;

import cn.hutool.core.lang.UUID;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.enums.UserIdentity;
import com.example.common.redis.service.RedisService;
import com.example.common.security.domain.LoginUser;
import com.example.common.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//操作用户Token
@Service
public class TokenService {
    @Autowired
    private RedisService redisService;
    public String createToken(Long userId,String secret,Integer identity){
        //生成jwt令牌的方法
        Map<String, Object> claims = new HashMap<>();
        String userKey = UUID.fastUUID().toString();
        claims.put(JwtConstants.LOGIN_USER_ID,userId);
        claims.put(JwtConstants.LOGIN_USER_KEY,userKey);
        String token = JwtUtils.createToken(claims, secret);
        //redis存储 敏感信息:身份字段 identity 1:普通用户 2:管理员
        //身份认证存储 常用数据结构 key value
        // key 保证唯一 :loginToken:userId 雪花算法
        String key = CacheConstants.LOGIN_TOKEN_KEY + userKey;
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        redisService.setCacheObject(key,loginUser,CacheConstants.EXP, TimeUnit.MINUTES);
        return token;
    }
}
