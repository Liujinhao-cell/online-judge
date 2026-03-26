package com.example.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.job.domain.message.MessageText;

import java.util.List;

public interface IMessageTextService extends IService<MessageText> {
    boolean batchInsert(List<MessageText> messageTextList);
}
