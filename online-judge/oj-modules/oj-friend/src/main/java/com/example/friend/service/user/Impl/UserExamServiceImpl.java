package com.example.friend.service.user.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.Constants;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.exception.ServiceException;
import com.example.common.security.service.TokenService;
import com.example.friend.domain.exam.Exam;
import com.example.friend.domain.user.UserExam;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.mapper.exam.ExamMapper;
import com.example.friend.mapper.user.UserExamMapper;
import com.example.friend.service.user.IUserExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserExamServiceImpl implements IUserExamService {
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private UserExamMapper userExamMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ExamCacheManager examCacheManager;
    @Value("${jwt.secret}")
    private String secret;
    @Override
    public int enter(String token,Long examId) {
        Exam exam = examMapper.selectById(examId);
        if(null == exam){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        //已开赛
        if(exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
//        Long userId = tokenService.getUserId(token,secret);
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //不能重复报名
        UserExam userExam = userExamMapper.selectOne(new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getExamId, examId)
                .eq(UserExam::getUserId, userId));
        if(userExam != null){
            throw new ServiceException(ResultCode.USER_EXAM_HAS_ENTER);
        }
        examCacheManager.addUserExamCache(userId,examId);
        userExam = new UserExam();
        userExam.setExamId(examId);
        userExam.setUserId(userId);
        return userExamMapper.insert(userExam);
    }
}
