package com.example.judge.service.Impl;

import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.judge.domain.SandBoxExecuteResult;
import com.example.judge.service.IJudgeService;
import com.example.judge.service.ISandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JudgeServiceImpl implements IJudgeService {
    @Autowired
    private ISandboxService sandboxService;
    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        SandBoxExecuteResult sandBoxExecuteResult =
                sandboxService.exeJavaCode(judgeSubmitDTO.getUserCode(),judgeSubmitDTO.getInputList());
        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();
        if(sandBoxExecuteResult != null && CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())){

        }else{
            userQuestionResultVO.setPass(Constants.FALSE);
            if(sandBoxExecuteResult != null) {
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
            }else{
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
        }
        return null;
    }
}
