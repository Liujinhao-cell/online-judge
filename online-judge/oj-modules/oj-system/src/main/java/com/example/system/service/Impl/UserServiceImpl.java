package com.example.system.service.Impl;

import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import com.example.system.domain.user.User;
import com.example.system.domain.user.dto.UserDTO;
import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.domain.user.vo.UserVO;
import com.example.system.manager.UserCacheManager;
import com.example.system.mapper.user.UserMapper;
import com.example.system.service.user.IUserService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCacheManager userCacheManager;
    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserDTO userDTO) {
        User user = userMapper.selectById(userDTO.getUserId());
        if(null == user){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userDTO.getStatus());
        userCacheManager.updateStatus(user.getUserId(),userDTO.getStatus());
        return userMapper.updateById(user);
    }
}
