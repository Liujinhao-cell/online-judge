package com.example.friend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.user.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
