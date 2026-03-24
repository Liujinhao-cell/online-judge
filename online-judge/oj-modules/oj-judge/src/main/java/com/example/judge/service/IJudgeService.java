package com.example.judge.service;


import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;

public interface IJudgeService {
    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}
