package com.example.system.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.core.constants.Constants;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import com.example.system.domain.exam.Exam;
import com.example.system.domain.exam.ExamQuestion;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamEditDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionAddDTO;
import com.example.system.domain.exam.vo.ExamDetailVO;
import com.example.system.domain.exam.vo.ExamVO;
import com.example.system.domain.question.Question;
import com.example.system.domain.question.vo.QuestionVO;
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
        Exam exam = getExam(examId);
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

    @Override
    public ExamDetailVO detail(Long examId) {
        ExamDetailVO examDetailVO = new ExamDetailVO();
        Exam exam = getExam(examId);
        BeanUtil.copyProperties(exam,examDetailVO);
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if(CollectionUtil.isEmpty(examQuestionList)){
            //不包含题目的竞赛：只有竞赛
            return examDetailVO;
        }
        List<Long> questionIdList = examQuestionList.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        //new LambdaQueryWrapper<Question>().in(数据表前面的属性,集合)
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId, Question::getTitle, Question::getDifficulty)
                .in(Question::getQuestionId, questionIdList));
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionList, QuestionVO.class);
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        //校验编辑竞赛存在性
        Exam exam = getExam(examEditDTO.getExamId());
        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        Long examId = examEditDTO.getExamId();
        //竞赛标题重复性，竞赛开始时间判断
        checkExamSaveParams(examEditDTO, examId);
        return examMapper.updateById(exam);
    }

    @Override
    public int detele(Long examId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        //删除竞赛包含的题目
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId,examId));
        return examMapper.deleteById(exam);
    }

    @Override
    public int publish(Long examId) {
        Exam exam = getExam(examId);
        Long count = examQuestionMapper.selectCount(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        if(count <= 0 || null == count){
            throw new ServiceException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }
        exam.setStatus(Constants.TRUE);
        return examMapper.updateById(exam);
    }

    @Override
    public int cancelPublish(Long examId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        exam.setStatus(Constants.FALSE);
        return examMapper.updateById(exam);
    }

    @Override
    public int questionDelete(Long examId, Long questionId) {
        Exam exam = getExam(examId);
        //判断竞赛是否已经开赛
        checkExam(exam);
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId,examId)
                .eq(ExamQuestion::getQuestionId,questionId));
    }

    /**
     * 检查竞赛时间是否晚于当前时间
     * @param exam 竞赛
     */
    private static void checkExam(Exam exam) {
        if(exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }

    private void checkExamSaveParams(ExamEditDTO examEditDTO, Long examId) {
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examEditDTO.getTitle())
                .ne(examId != null,Exam::getExamId, examId)); // 排除自身Id
        if(CollectionUtil.isNotEmpty(exams)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        if(examEditDTO.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if(!examEditDTO.getEndTime().isAfter(examEditDTO.getStartTime())){
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
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

    private Exam getExam(Long examId) {
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
