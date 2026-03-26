package com.example.job.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.Constants;
import com.example.common.redis.service.RedisService;
import com.example.job.domain.exam.Exam;
import com.example.job.domain.message.Message;
import com.example.job.domain.message.MessageText;
import com.example.job.domain.message.vo.MessageTextVO;
import com.example.job.domain.user.UserScore;
import com.example.job.mapper.exam.ExamMapper;
import com.example.job.mapper.message.MessageTextMapper;
import com.example.job.mapper.user.UserSubmitMapper;
import com.example.job.service.IMessageService;
import com.example.job.service.IMessageTextService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExamXxlJob {
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Autowired
    private MessageTextMapper messageTextMapper;
    @Autowired
    private IMessageTextService messageTextService;
    @Autowired
    private IMessageService messageService;
    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler(){
        //哪些竞赛应该存入历史竞赛列表 未完赛列表
        log.info("*** examListOrganizeHandler ***");
        List<Exam> unFinishList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .gt(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(unFinishList,CacheConstants.EXAM_UNFINISHED_LIST);
        List<Exam> historyList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .le(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));
        refreshCache(historyList,CacheConstants.EXAM_HISTORY_LIST);
    }

    @XxlJob("examResultHandler")
    public void examResultHandler(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minusDatetime = now.minusDays(1);
        List<Exam> examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId)
                .eq(Exam::getStatus, Constants.TRUE)
                .ge(Exam::getEndTime, minusDatetime)
                .le(Exam::getEndTime, now));
        if(CollectionUtil.isEmpty(examList)){
            return;
        }
        Set<Long> examIdSet= examList.stream().map(Exam::getExamId).collect(Collectors.toSet());
        List<UserScore> userScoreList = userSubmitMapper.selectUserScoreList(examIdSet);
        Map<Long, List<UserScore>> userScoreMap = userScoreList.stream().collect(Collectors.groupingBy(UserScore::getExamId));
        createMessage(userScoreMap,examList);

    }

    private void createMessage(Map<Long, List<UserScore>> userScoreMap,List<Exam> examList) {
        List<MessageText> messageTextList = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();
        for(Exam exam:examList){
            Long examId = exam.getExamId();
            List<UserScore> userScoreList = userScoreMap.get(examId);
            int totalUser = userScoreList.size();
            int examRank = 1;
            for(UserScore userScore:userScoreList){
                String msgTitle = exam.getTitle() +"——排名情况";
                String msgContent = "您所参与的竞赛:"+exam.getTitle() +
                        ",本次参与竞赛一共"+totalUser+"人，您排名第"+examRank +"名";
                MessageText messageText = new MessageText();
                messageText.setMessageContent(msgContent);
                messageText.setMessageTitle(msgTitle);
                messageText.setCreateBy(Constants.SYSTEM_USER_ID);
                messageTextList.add(messageText);
                Message message = new Message();
                message.setSendId(Constants.SYSTEM_USER_ID);
                message.setCreateBy(Constants.SYSTEM_USER_ID);
                message.setRecId(userScore.getUserId());
                examRank++;
            }
        }
        messageTextService.batchInsert(messageTextList);
        Map<String,MessageTextVO> messageTextVOMap = new HashMap<>();
        for(int i=0;i<messageList.size();i++){
            MessageText messageText = messageTextList.get(i);
            MessageTextVO messageTextVO = new MessageTextVO();
            BeanUtil.copyProperties(messageText,messageTextVO);
            String detailKey = getDetailKey(messageText.getTextId());
            messageTextVOMap.put(detailKey,messageTextVO);
            Message message = messageList.get(i);
            message.setTextId(messageText.getTextId());
        }
        messageService.batchInsert(messageList);
        Map<Long, List<Message>> userMsgMap = messageList.stream().collect(Collectors.groupingBy(Message::getRecId));
        Iterator<Map.Entry<Long, List<Message>>> iterator = userMsgMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Long, List<Message>> entry = iterator.next();
            Long recId = entry.getKey();
            String userMsgListKey = getUserMsgListKey(recId);
            List<Long> userMsgTextIdList = entry.getValue().stream().map(Message::getTextId).toList();
            //用户消息列表
            redisService.rightPushAll(userMsgListKey,userMsgTextIdList);
        }
        redisService.multiSet(messageTextVOMap);
    }

    public void refreshCache(List<Exam> examList,String examListKey) {
            //缓存存放
            if (CollectionUtil.isEmpty(examList)) {
                return;
            }
            Map<String, Exam> examMap = new HashMap<>();
            List<Long> examIdList = new ArrayList<>();
            for (Exam exam : examList) {
                examMap.put(getDetailKey(exam.getExamId()), exam);
                examIdList.add(exam.getExamId());
            }
            redisService.multiSet(examMap);  //刷新详情缓存
            redisService.deleteObject(examListKey);
        redisService.rightPushAll(examListKey, examIdList);      //刷新列表缓存
    }
    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getUserMsgListKey(Long userId) {
        return CacheConstants.USER_MESSAGE_LIST + userId;
    }

    private String getMsgDetailKey(Long textId) {
        return CacheConstants.MESSAGE_DETAIL;
    }
}
