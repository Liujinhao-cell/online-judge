package com.example.friend.mapper.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.message.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
