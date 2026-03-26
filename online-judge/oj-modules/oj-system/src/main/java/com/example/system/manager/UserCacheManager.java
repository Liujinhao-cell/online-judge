package com.example.system.manager;

import com.example.common.core.constants.CacheConstants;
import com.example.common.redis.service.RedisService;
import com.example.system.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {
    @Autowired
    private RedisService redisService;
    public void updateStatus(Long userId,Integer status){
        //用户缓存
        String userKey = getUserKey(userId);
        User user = redisService.getCacheObject(userKey, User.class);
        if(null == user){
            return;
        }
        user.setStatus(status);
        redisService.setCacheObject(userKey,user);
        //10分钟
        redisService.expire(userKey,CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }
    private String getUserKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}
