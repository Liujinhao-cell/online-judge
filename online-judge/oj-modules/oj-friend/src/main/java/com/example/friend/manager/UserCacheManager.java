package com.example.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.redis.service.RedisService;
import com.example.friend.domain.user.User;
import com.example.friend.domain.user.vo.UserVO;
import com.example.friend.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserMapper userMapper;
    public UserVO getUserId(Long userId){
        String userKey = getUserKey(userId);
        UserVO userVO = redisService.getCacheObject(userKey, UserVO.class);
        if(null != userVO){
            //访问用户量大后需要更多key
            redisService.expire(userKey,CacheConstants.USER_EXP, TimeUnit.MINUTES);
            return userVO;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId,
                        User::getNickName,
                        User::getHeadImage,
                        User::getSex,
                        User::getEmail,
                        User::getPhone,
                        User::getWechat,
                        User::getIntroduce,
                        User::getSchoolName,
                        User::getMajorName,
                        User::getStatus)
                .eq(User::getUserId, userId));
        if(null == user){
            return null;
        }
        refreshUser(user);
        userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    public void refreshUser(User user){
        //用户缓存
        String userKey = getUserKey(user.getUserId());
        redisService.setCacheObject(userKey,user);
        //10分钟
        redisService.expire(userKey,CacheConstants.USER_EXP, TimeUnit.MINUTES);
    }
    private String getUserKey(Long userId) {
        return CacheConstants.USER_DETAIL + userId;
    }
}
