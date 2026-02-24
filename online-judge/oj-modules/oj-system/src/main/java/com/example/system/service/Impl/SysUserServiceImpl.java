package com.example.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.domain.R;
import com.example.common.core.domain.enums.ResultCode;
import com.example.system.controller.result.LoginResult;
import com.example.system.domain.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    /**
     * 查询用户信息
     * @param userAccount
     * @param password
     * @return {@link LoginResult }
     */
    @Override
    public R<Void> login(String userAccount, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getPassword).eq(SysUser::getUserAccount, userAccount));
        R loginResult = new R();
        if(null == sysUser){
            loginResult.setCode(ResultCode.AILED_USER_EXISTS.getCode());
            loginResult.setMsg(ResultCode.AILED_USER_EXISTS.getMsg());
            return loginResult;
        }
        if(!sysUser.getPassword().equals(password)){
            loginResult.setCode(ResultCode.FAILED_LOGIN.getCode());
            loginResult.setMsg(ResultCode.FAILED_LOGIN.getMsg());
            return loginResult;
        }
        loginResult.setCode(ResultCode.SUCCESS.getCode());
        loginResult.setMsg(ResultCode.SUCCESS.getMsg());
        return loginResult;
    }
}
