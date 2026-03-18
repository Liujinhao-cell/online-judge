package com.example.common.security.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.redis.service.RedisService;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//操作用户Token
@Service
@Slf4j
public class TokenService {
    @Autowired
    private RedisService redisService;

    public String createToken(Long userId, String secret, Integer identity,String nickName,String headImage) {
        if (userId == null) {
            log.error("创建token失败：userId不能为空");
            throw new IllegalArgumentException("userId不能为空");
        }
        if (StrUtil.isEmpty(secret)) {
            log.error("创建token失败：secret不能为空");
            throw new IllegalArgumentException("secret不能为空");
        }
        //生成jwt令牌的方法
        Map<String, Object> claims = new HashMap<>();
        String userKey = UUID.fastUUID().toString();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KEY, userKey);
        String token = JwtUtils.createToken(claims, secret);
        //redis存储 敏感信息:身份字段 identity 1:普通用户 2:管理员
        //身份认证存储 常用数据结构 key value
        // key 保证唯一 :loginToken:userId 雪花算法
        String tokenKey = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        loginUser.setHeadImage(headImage);
        redisService.setCacheObject(tokenKey, loginUser, CacheConstants.EXP, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 延长Token的有效时间：延长redis中存储用于用户身份认证的有效时间
     * 身份认证后进行，请求到达Controller之前
     *
     * @param token 令牌
     * @param secret 密钥
     */
    public void extendToken(String token,String secret) {
//        Claims claims;
//        try {
//            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
//            if (claims == null) {
//                log.error("解析token:{},出现异常", token);
//                return;
//            }
//        } catch (Exception e) {
//            log.error("解析token:{},出现异常", token, e);
//            return;
//        }
//        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key
        String cleanToken = cleanTokenPrefix(token);
        String userKey = getUserKey(cleanToken, secret);
        if (null == userKey) {
            log.error("解析token:{},出现异常", token);
            return;
        }
        String tokenKey = getTokenKey(userKey);
        //延长有效时间为12个小时:小于180min时延长
        Long expire = redisService.getExpire(tokenKey, TimeUnit.MINUTES);
        if (null != expire && expire < CacheConstants.REFRESH_TIME) {
            redisService.expire(tokenKey, CacheConstants.EXP, TimeUnit.MINUTES);
        }
    }

    private String getTokenKey(String userKey) {
        return CacheConstants.LOGIN_TOKEN_KEY + userKey;
    }

    public LoginUser getLoginUser(String token,String secret) {
        String cleanToken = cleanTokenPrefix(token);
        String userKey = getUserKey(cleanToken, secret);
        if(null == userKey){
            log.error("解析token:{},出现异常", token);
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey), LoginUser.class);
    }

    /**
     * 退出登录：删除key
     * @param token
     * @param secret
     */
    public boolean deleteLoginUser(String token, String secret) {
        //解析token,拿取数据部分
        String userKey = getUserKey(token, secret);
        if(null == userKey){
            log.error("解析token:{},出现异常", token);
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }
    // ====== Token前缀清理方法 ======
    private String cleanTokenPrefix(String token) {
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        // 移除Bearer前缀（不区分大小写）
        token = token.trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return StrUtil.isEmpty(token) ? null : token;
    }

    public Long getUserId(Claims claims){
        if (claims == null) return null;
        return Long.valueOf(JwtUtils.getUserId(claims)); //获取jwt中的key
    }
    private String getUserKey(Claims claims) {
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims); //获取jwt中的key
    }
    private String getUserKey(String token, String secret) {
        Claims claims = getClaims(token, secret);
        if (claims == null) return null;
        return JwtUtils.getUserKey(claims); //获取jwt中的key
    }

    public Claims getClaims(String token, String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            if (claims == null) {
                log.error("解析token:{},出现异常", token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token:{},出现异常", token, e);
            return null;
        }
        return claims;
    }

}
