package com.example.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.job.domain.message.Message;

import java.util.List;

public interface IMessageService extends IService<Message> {
    boolean batchInsert(List<Message> messageList);
}
