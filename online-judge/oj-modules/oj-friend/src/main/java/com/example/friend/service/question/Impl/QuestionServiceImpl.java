package com.example.friend.service.question.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.question.Question;
import com.example.friend.domain.question.dto.QuestionQueryDTO;
import com.example.friend.domain.question.es.QuestionES;
import com.example.friend.domain.question.vo.QuestionDetailVO;
import com.example.friend.domain.question.vo.QuestionVO;
import com.example.friend.manager.QuestionCacheManager;
import com.example.friend.mapper.question.QuestionMapper;
import com.example.friend.mapper.question.QuestionRepository;
import com.example.friend.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements IQuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionCacheManager questionCacheManager;
    @Override
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        //先查ES是否有数据
        long count = questionRepository.count();
        if(count <= 0){
            //查询数据库
            refreshQuestion();
        }
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        Pageable pageable = PageRequest.of(questionQueryDTO.getPageNum()-1
                , questionQueryDTO.getPageSize(),sort);
        Integer difficulty = questionQueryDTO.getDifficulty();
        String keyword = questionQueryDTO.getKeyword();
        Page<QuestionES> questionESPage;
        if(null == difficulty && StrUtil.isEmpty(keyword)){
            //普通查询
            questionESPage = questionRepository.findAll(pageable);
        }else if(StrUtil.isEmpty(keyword)){ //difficulty查
            questionESPage = questionRepository.findByDifficulty(difficulty,pageable);
        }else if(difficulty == null){ //keyword查
            questionESPage = questionRepository.findByTitleOrContent(keyword,keyword,pageable);
        }else{
            questionESPage = questionRepository.findByTitleOrContentAndDifficulty(keyword,keyword,difficulty,pageable);
        }
        long totalElements = questionESPage.getTotalElements();
        if(totalElements == 0){
            return TableDataInfo.empty();
        }
        List<QuestionES> content = questionESPage.getContent();
        List<QuestionVO> questionVOList = BeanUtil.copyToList(content, QuestionVO.class);
        return TableDataInfo.success(questionVOList,totalElements);
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        if(questionES != null){
            BeanUtil.copyProperties(questionES,questionDetailVO);
            return questionDetailVO;
        }
        Question question = questionMapper.selectById(questionId);
        if(null == question){
            return null;
        }
        //刷新ES中的数据
        refreshQuestion();
        BeanUtil.copyProperties(question,questionDetailVO);
        return questionDetailVO;
    }

    @Override
    public String preQuestion(Long questionId) {
        Long listSize = questionCacheManager.getListSize();
        if(null == listSize || listSize <= 0){
            questionCacheManager.refreshCache();
        }
        return questionCacheManager.preQuestion(questionId).toString();
    }

    @Override
    public String nextQuestion(Long questionId) {
        Long listSize = questionCacheManager.getListSize();
        if(null == listSize || listSize <= 0){
            questionCacheManager.refreshCache();
        }
        return questionCacheManager.nextQuestion(questionId).toString();
    }

    private void refreshQuestion() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        if(CollectionUtil.isEmpty(questionList)){
            return;
        }
        List<QuestionES> questionESList = BeanUtil.copyToList(questionList, QuestionES.class);
        //同步给ES
        questionRepository.saveAll(questionESList);
    }
}
