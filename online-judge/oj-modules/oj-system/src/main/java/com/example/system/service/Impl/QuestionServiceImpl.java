package com.example.system.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.TableDataInfo;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import com.example.system.domain.question.Question;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionDetailVO;
import com.example.system.domain.question.vo.QuestionVO;
import com.example.system.mapper.question.QuestionMapper;
import com.example.system.service.question.QuestionService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<QuestionVO> list(QuestionQueryDTO questionQueryDTO) {
        String excludeIdStr = questionQueryDTO.getExcludeIdStr();
        if(StrUtil.isNotEmpty(excludeIdStr)){
            String[] excludeIdArr = excludeIdStr.split(Constants.SPLIT_SEM);
            // String -> long 去重
            Set<Long> excludeIdSet = Arrays.stream(excludeIdArr)
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());
            questionQueryDTO.setExcludeIdSet(excludeIdSet);
        }
        PageHelper.startPage(questionQueryDTO.getPageNum(), questionQueryDTO.getPageSize());
        return questionMapper.selectQuestionList(questionQueryDTO);
    }

    @Override
    public int add(QuestionAddDTO questionAddDTO) {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle, questionAddDTO.getTitle()));
        if(CollectionUtil.isNotEmpty(questionList)){
            //资源已存在
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO,question);
        // 兜底默认值
        if (question.getTimeLimit() == null) {
            question.setTimeLimit(10000L); // 默认10秒
        }
        if (question.getSpaceLimit() == null) {
            question.setSpaceLimit(256L); // 默认256MB
        }
        return questionMapper.insert(question);
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        Question question = questionMapper.selectById(questionId);

        extracted(questionId);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtil.copyProperties(question,questionDetailVO);
        return questionDetailVO;
    }

    @Override
    public int edit(QuestionEditDTO questionEditDTO) {
        Long questionId = questionEditDTO.getQuestionId();
        Question oldQuestion = questionMapper.selectById(questionId);
        if(null == oldQuestion){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }

        if (questionEditDTO.getTitle() != null) {
            oldQuestion.setTitle(questionEditDTO.getTitle());
        }
        if (questionEditDTO.getDifficulty() != null) {
            oldQuestion.setDifficulty(questionEditDTO.getDifficulty());
        }
        if (questionEditDTO.getTimeLimit() != null) {
            oldQuestion.setTimeLimit(questionEditDTO.getTimeLimit());
        }
        if (questionEditDTO.getSpaceLimit() != null) {
            oldQuestion.setSpaceLimit(questionEditDTO.getSpaceLimit());
        }
        if (questionEditDTO.getContent() != null) {
            oldQuestion.setContent(questionEditDTO.getContent());
        }
        if (questionEditDTO.getQuestionCase() != null) {
            oldQuestion.setQuestionCase(questionEditDTO.getQuestionCase());
        }
        if (questionEditDTO.getDefaultCode() != null) {
            oldQuestion.setDefaultCode(questionEditDTO.getDefaultCode());
        }
        if (questionEditDTO.getMainFuc() != null) {
            oldQuestion.setMainFuc(questionEditDTO.getMainFuc());
        }

        oldQuestion.setUpdateTime(LocalDateTime.now());
        oldQuestion.setUpdateBy(getCurrentUserId()); // 需要实现获取当前用户ID的方法

        return questionMapper.updateById(oldQuestion);
    }
    // 获取当前用户ID的方法
    private Long getCurrentUserId() {
        // 从 SecurityContext 获取当前用户ID
        // return SecurityUtils.getCurrentUserId();
        return 2029939600112037890L; // 临时使用默认值
    }

    @Override
    public int delete(Long questionId) {
        extracted(questionId);
        return questionMapper.deleteById(questionId);
    }

    /**
     * 校验questionId是否重复
     * @param questionId 题目id
     */
    private void extracted(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        //Id错误未查到
        if(null == question){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
    }
}
