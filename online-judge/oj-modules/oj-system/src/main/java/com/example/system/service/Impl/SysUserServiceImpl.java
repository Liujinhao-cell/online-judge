package com.example.system.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.HttpConstants;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.enums.UserIdentity;
import com.example.common.security.exception.ServiceException;
import com.example.common.security.service.TokenService;
import com.example.system.controller.result.LoginResult;
import com.example.system.domain.sysuser.SysUser;
import com.example.system.domain.sysuser.SysUserSaveDTO;
import com.example.system.mapper.sysuser.SysUserMapper;
import com.example.system.service.sysuser.SysUserService;
import com.example.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .select(SysUser::getPassword,SysUser::getNickName).eq(SysUser::getUserAccount, userAccount));
        if(null == sysUser){
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(!BCryptUtils.matchesPassword(password,sysUser.getPassword())){
            return R.fail(ResultCode.FAILED_LOGIN);
        }
        System.out.println(sysUser.getUserId());
        //生成jwt令牌的方法
        String token = tokenService.createToken(sysUser.getUserId(),
                secret, UserIdentity.ADMIN.getValue(),sysUser.getNickName());
        return R.ok(token);
    }

    @Override
    public boolean logout(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return tokenService.deleteLoginUser(token, secret);
    }
    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {
        //DTO -> ENTITY
        //校验重复帐户
        List<SysUser> sysUserList = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, sysUserSaveDTO.getUserAccount()));
        if(CollectionUtil.isNotEmpty(sysUserList)){
            //用户已经存在
            //自定义异常
            throw new ServiceException(ResultCode.AILED_USER_EXISTS);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(sysUserSaveDTO.getUserAccount());
        sysUser.setPassword(BCryptUtils.encryptPassword(sysUserSaveDTO.getPassword()));
        return sysUserMapper.insert(sysUser);
    }

    @Override
    public R<LoginUserVO> info(String token) {
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if(null == loginUser){
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        return R.ok(loginUserVO);
    }
}
