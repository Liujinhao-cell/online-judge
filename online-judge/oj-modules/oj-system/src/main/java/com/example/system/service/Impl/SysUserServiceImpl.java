package com.example.system.service.Impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.R;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.enums.UserIdentity;
import com.example.common.redis.service.RedisService;
import com.example.common.security.service.TokenService;
import com.example.common.security.utils.JwtUtils;
import com.example.system.controller.result.LoginResult;
import com.example.system.domain.LoginUser;
import com.example.system.domain.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.service.SysUserService;
import com.example.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RefreshScope
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private TokenService tokenService;
    @Value("${jwt.secret}")
    private String secret;
    /**
     * 查询用户信息
     * @param userAccount
     * @param password
     * @return {@link LoginResult }
     */
    @Override
    public R<String> login(String userAccount, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getPassword).eq(SysUser::getUserAccount, userAccount));
        if(null == sysUser){
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(!BCryptUtils.matchesPassword(password,sysUser.getPassword())){
            return R.fail(ResultCode.FAILED_LOGIN);
        }
        //生成jwt令牌的方法
        String token = tokenService.createToken(sysUser.getUserId(), secret, UserIdentity.ADMIN.getValue());
        return R.ok(token);
    }
}
