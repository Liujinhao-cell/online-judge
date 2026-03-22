package com.example.friend.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.question.Question;
import com.example.friend.mapper.question.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionCacheManager {
    @Autowired
    private RedisService redisService;
    @Autowired
    private QuestionMapper questionMapper;
    /**
     * 获取redis列表大小
     *
     * @return {@link Long }
     */
    public Long getListSize() {
        Long size = redisService.getListSize(CacheConstants.QUESTION_LIST);
        return size != null ? size : 0L;
    }

    public void refreshCache() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId)
                .orderByDesc(Question::getCreateTime));
        if(CollectionUtil.isEmpty(questionList)){
            return;
        }
        List<Long> questionIdList = questionList.stream().map(Question::getQuestionId).toList();
        redisService.rightPushAll(CacheConstants.QUESTION_LIST,questionIdList);
    }

    public Long preQuestion(Long questionId) {
        //先加载redis中全量数据库的内容
//        List<Long> list = redisService.getCacheListByRange(CacheConstants.QUESTION_LIST, 0, -1, Long.class);
//        Question question = questionMapper.selectById(questionId);
//        Long index = list.indexOf(question);
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        if(index == 0){
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index - 1, Long.class);
    }

    public Long nextQuestion(Long questionId) {
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST, questionId);
        Long lastIndex = getListSize() - 1;
        if(index == lastIndex){
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return redisService.indexForList(CacheConstants.QUESTION_LIST, index + 1, Long.class);
    }
}
