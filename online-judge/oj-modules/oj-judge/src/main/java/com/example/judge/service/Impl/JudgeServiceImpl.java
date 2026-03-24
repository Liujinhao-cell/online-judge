package com.example.judge.service.Impl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.api.UserExeResult;
import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.constants.Constants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.enums.CodeRunStatus;
import com.example.judge.domain.SandBoxExecuteResult;
import com.example.judge.domain.UserSubmit;
import com.example.judge.mapper.UserSubmitMapper;
import com.example.judge.service.IJudgeService;
import com.example.judge.service.ISandboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JudgeServiceImpl implements IJudgeService {
    @Autowired
    private ISandboxService sandboxService;
    @Autowired
    private UserSubmitMapper userSubmitMapper;
    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        SandBoxExecuteResult sandBoxExecuteResult =
                sandboxService.exeJavaCode(judgeSubmitDTO.getUserId(),judgeSubmitDTO.getUserCode(),judgeSubmitDTO.getInputList());
        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();
        if(sandBoxExecuteResult != null && CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())){
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO);
        }else{
            userQuestionResultVO.setPass(Constants.FALSE);
            if(sandBoxExecuteResult != null) {
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
            }else{
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
        }
        saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);
        return userQuestionResultVO;
    }

    private static UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO,
                                SandBoxExecuteResult sandBoxExecuteResult,
                                UserQuestionResultVO userQuestionResultVO) {
        //比对结果，时间限制，空间限制
        List<String> exeOutputList = sandBoxExecuteResult.getOutputList();
        List<String> outputList = judgeSubmitDTO.getOutputList();
        if(outputList.size() != exeOutputList.size()){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
            List<UserExeResult> userExeResultList = new ArrayList<>();
        boolean passed = resultCompare(judgeSubmitDTO, outputList, exeOutputList, userExeResultList);
        return assembleUserQuestionResultVO(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO, userExeResultList, passed);
    }

    private static UserQuestionResultVO assembleUserQuestionResultVO(JudgeSubmitDTO judgeSubmitDTO,
                                                                     SandBoxExecuteResult sandBoxExecuteResult,
                                                                     UserQuestionResultVO userQuestionResultVO,
                                                                     List<UserExeResult> userExeResultList,
                                                                     boolean passed) {
        userQuestionResultVO.setUserExeResultList(userExeResultList);
        if(!passed){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
        //判断空间限制
        if(sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_MEMORY.getMsg());
            return userQuestionResultVO;
        }
        //判断时间限制
        if(sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_TIME.getMsg());
            return userQuestionResultVO;
        }
        //结果正确
        userQuestionResultVO.setPass(Constants.TRUE);
        int score = judgeSubmitDTO.getDifficulty() * JudgeConstants.DEFAULT_SCORE;
        userQuestionResultVO.setScore(score);
        return userQuestionResultVO;
    }

    private static boolean resultCompare(JudgeSubmitDTO judgeSubmitDTO,
                                         List<String> outputList, List<String> exeOutputList, List<UserExeResult> userExeResultList) {
        boolean passed = true;
        for(int index = 0; index< outputList.size(); index++){
            String output = outputList.get(index);
            String exeOutput = exeOutputList.get(index);
            String input = judgeSubmitDTO.getInputList().get(index);
            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(output);
            userExeResult.setExeOut(exeOutput);
            userExeResultList.add(userExeResult);
            if(output.equals(exeOutput)){
                passed = false;
            }
        }
        return passed;
    }

    private void saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit();
        BeanUtil.copyProperties(userQuestionResultVO,userSubmit);
        userSubmit.setUserId(judgeSubmitDTO.getUserId());
        userSubmit.setQuestionId(judgeSubmitDTO.getQuestionId());
        userSubmit.setExamId(judgeSubmitDTO.getExamId());
        userSubmit.setProgramType(judgeSubmitDTO.getProgramType());
        userSubmit.setUserCode(judgeSubmitDTO.getUserCode());
        //插入用户最后一条记录
        userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getQuestionId, judgeSubmitDTO.getQuestionId())
                .eq(UserSubmit::getUserId, judgeSubmitDTO.getUserId())
                .isNull(judgeSubmitDTO.getExamId() == null ,UserSubmit::getExamId)
                .eq(UserSubmit::getExamId, judgeSubmitDTO.getExamId())
        );
        userSubmitMapper.insert(userSubmit);
    }
}
