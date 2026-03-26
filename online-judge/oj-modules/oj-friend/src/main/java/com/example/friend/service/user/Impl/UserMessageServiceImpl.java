package com.example.friend.service.user.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.TableDataInfo;
import com.example.common.core.enums.ExamListType;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.domain.exam.vo.ExamVO;
import com.example.friend.domain.message.vo.MessageTextVO;
import com.example.friend.manager.MessageCacheManager;
import com.example.friend.mapper.message.MessageMapper;
import com.example.friend.mapper.message.MessageTextMapper;
import com.example.friend.service.user.IUserMessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserMessageServiceImpl implements IUserMessageService {
    @Autowired
    private MessageCacheManager messageCacheManager;
    @Autowired
    private MessageTextMapper messageTextMapper;
    @Override
    public TableDataInfo list(PageQueryDTO dto) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //redis中获取之前竞赛列表的数据
        Long total = messageCacheManager.getListSize(userId);
        List<MessageTextVO> messageTextVOList;
        //redis中无数据
        if (null == total || total <= 0) {
            //从数据库中查询并同步
            PageHelper.startPage(dto.getPageNum(),dto.getPageSize());
            messageTextVOList = messageTextMapper.selectUserMessageList(userId);
            messageCacheManager.refreshCache(userId);
            total = new PageInfo<>(messageTextVOList).getTotal();
        } else {
            messageTextVOList = messageCacheManager.getMessageTextVOList(dto,userId);
        }
        if (CollectionUtil.isEmpty(messageTextVOList)) {
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(messageTextVOList, total);
    }
}
