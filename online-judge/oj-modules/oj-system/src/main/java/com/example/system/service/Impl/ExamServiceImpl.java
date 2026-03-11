package com.example.system.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import com.example.system.domain.exam.Exam;
import com.example.system.domain.exam.ExamQuestion;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionAddDTO;
import com.example.system.domain.exam.vo.ExamVO;
import com.example.system.domain.question.Question;
import com.example.system.mapper.exam.ExamMapper;
import com.example.system.mapper.exam.ExamQuestionMapper;
import com.example.system.mapper.question.QuestionMapper;
import com.example.system.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper,ExamQuestion> implements IExamService {
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public int add(ExamAddDTO examAddDTO) {
        //查询竞赛重复性
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examAddDTO.getTitle()));
        if(CollectionUtil.isNotEmpty(exams)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if(examAddDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if(!examAddDTO.getEndTime().isAfter(examAddDTO.getStartTime())){
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
        Exam exam = new Exam();
        exam.setTitle(examAddDTO.getTitle());
        exam.setStartTime(examAddDTO.getStartTime());
        exam.setEndTime(examAddDTO.getEndTime());
        exam.setStatus(0);
        return examMapper.insert(exam);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean questionAdd(ExamQuestionAddDTO examQuestionAddDTO) {
        Long examId = examQuestionAddDTO.getExamId();
        //1.竞赛重复性校验
        Exam exam = getExam(examQuestionAddDTO);
        //2.已开始竞赛不能添加
        if (exam.getStatus() == 1) {  // 假设1表示已开始
            throw new ServiceException(ResultCode.EXAM_ALREADY_STARTED_QUESTION_CAN_NOT_ADD);
        }
        //3.题目需去重 -> set
        Set<Long> questionIdSet = examQuestionAddDTO.getQuestionIdSet();
        if(CollectionUtil.isEmpty(questionIdSet)){
            //未添加任何题目
            return true;
        }
        //4。校验题目存在性
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
        if(CollectionUtil.isEmpty(questionList) || questionList.size() < questionIdSet.size()){
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }
        //5.校验题目是否已经在竞赛中
        checkQuestionNotExistsInExam(questionIdSet, examId);
        //6.数据库批量插入
        return saveExamQuestion(questionIdSet, examId);
    }

    private boolean saveExamQuestion(Set<Long> questionIdSet, Long examId) {
        int num = 1;
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for(Long questionId: questionIdSet){
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setQuestionId(questionId);
            examQuestion.setExamId(examId);
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }
        //批量插入数据
        return saveBatch(examQuestionList);
    }

    private Exam getExam(ExamQuestionAddDTO examQuestionAddDTO) {
        Long examId = examQuestionAddDTO.getExamId();
        Exam exam = examMapper.selectById(examId);
        if(null == exam){
            throw new ServiceException(ResultCode.EXAM_NOT_EXISTS);
        }
        return exam;
    }
    private void checkQuestionNotExistsInExam(Set<Long> questionIdSet, Long examId) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getExamId, examId)
                .in(ExamQuestion::getQuestionId, questionIdSet);

        Long existsCount = examQuestionMapper.selectCount(wrapper);
        if (existsCount > 0) {
            throw new ServiceException(ResultCode.QUESTION_ALREADY_IN_EXAM);
        }
    }
    private void validateQuestionsExist(Set<Long> questionIdSet) {
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);

        // 转换为Map便于查找
        Map<Long, Question> questionMap = questionList.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));

        // 找出不存在的题目ID
        List<Long> notExistIds = questionIdSet.stream()
                .filter(id -> !questionMap.containsKey(id))
                .collect(Collectors.toList());

        if (!notExistIds.isEmpty()) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
    }
}
