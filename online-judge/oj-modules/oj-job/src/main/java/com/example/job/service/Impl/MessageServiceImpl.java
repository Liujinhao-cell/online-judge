package com.example.job.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.job.domain.message.Message;
import com.example.job.domain.message.MessageText;
import com.example.job.mapper.message.MessageMapper;
import com.example.job.service.IMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    @Override
    public boolean batchInsert(List<Message> messageList){
        return saveBatch(messageList);
    }
}
