package com.example.job.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.job.domain.message.MessageText;
import com.example.job.mapper.message.MessageTextMapper;
import com.example.job.service.IMessageTextService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageTextServiceImpl extends ServiceImpl<MessageTextMapper,MessageText> implements IMessageTextService {
    @Override
    public boolean batchInsert(List<MessageText> messageTextList){
        return saveBatch(messageTextList);
    }
}
