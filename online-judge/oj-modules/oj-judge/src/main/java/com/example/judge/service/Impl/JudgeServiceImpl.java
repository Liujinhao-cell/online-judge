package com.example.judge.service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
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
import com.example.judge.service.ISandboxPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JudgeServiceImpl implements IJudgeService {

    @Autowired
    private ISandboxPoolService sandboxPoolService;
    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        log.info("开始判题: userId={}, questionId={}, examId={}",
                judgeSubmitDTO.getUserId(),
                judgeSubmitDTO.getQuestionId(),
                judgeSubmitDTO.getExamId());

        SandBoxExecuteResult sandBoxExecuteResult = null;
        try {
            // 使用容器池执行代码
            sandBoxExecuteResult = sandboxPoolService.exeJavaCode(
                    judgeSubmitDTO.getUserId(),
                    judgeSubmitDTO.getUserCode(),
                    judgeSubmitDTO.getInputList()
            );
        } catch (Exception e) {
            log.error("执行代码异常", e);
            sandBoxExecuteResult = SandBoxExecuteResult.fail(
                    CodeRunStatus.UNKNOWN_FAILED,
                    "执行代码异常: " + e.getMessage()
            );
        }

        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();

        if (sandBoxExecuteResult != null && CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())) {
            // 判题成功，进行结果比对
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO);
            log.info("判题成功: userId={}, score={}, pass={}",
                    judgeSubmitDTO.getUserId(),
                    userQuestionResultVO.getScore(),
                    userQuestionResultVO.getPass());
        } else {
            // 判题失败
            userQuestionResultVO.setPass(Constants.FALSE);
            if (sandBoxExecuteResult != null) {
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
                log.warn("判题失败: userId={}, message={}",
                        judgeSubmitDTO.getUserId(),
                        sandBoxExecuteResult.getExeMessage());
            } else {
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
                log.error("判题失败: 执行结果为空");
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
        }

        // 保存提交记录
        saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);

        return userQuestionResultVO;
    }

    private static UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO,
                                                SandBoxExecuteResult sandBoxExecuteResult,
                                                UserQuestionResultVO userQuestionResultVO) {
        // 比对结果，时间限制，空间限制
        List<String> exeOutputList = sandBoxExecuteResult.getOutputList();
        List<String> outputList = judgeSubmitDTO.getOutputList();

        if (exeOutputList == null) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage("执行结果为空");
            return userQuestionResultVO;
        }

        if (outputList.size() != exeOutputList.size()) {
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

        if (!passed) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }

        // 判断空间限制
        if (sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_MEMORY.getMsg());
            return userQuestionResultVO;
        }

        // 判断时间限制
        if (sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()) {
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_TIME.getMsg());
            return userQuestionResultVO;
        }

        // 结果正确
        userQuestionResultVO.setPass(Constants.TRUE);
        int score = judgeSubmitDTO.getDifficulty() * JudgeConstants.DEFAULT_SCORE;
        userQuestionResultVO.setScore(score);
        userQuestionResultVO.setExeMessage("判题通过");

        return userQuestionResultVO;
    }

    private static boolean resultCompare(JudgeSubmitDTO judgeSubmitDTO,
                                         List<String> outputList,
                                         List<String> exeOutputList,
                                         List<UserExeResult> userExeResultList) {
        boolean allPassed = true;

        for (int index = 0; index < outputList.size(); index++) {
            String expectedOutput = outputList.get(index);
            String actualOutput = exeOutputList.get(index);
            String input = judgeSubmitDTO.getInputList().get(index);

            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(expectedOutput);
            userExeResult.setExeOut(actualOutput);
            userExeResultList.add(userExeResult);

            // 标准化输出格式：移除所有空格进行比对
            String normalizedExpected = expectedOutput.replaceAll("\\s+", "");
            String normalizedActual = actualOutput.replaceAll("\\s+", "");

            if (normalizedActual.equals(normalizedExpected)) {
                log.debug("测试用例 {} 通过: 期望={}, 实际={}", index + 1, expectedOutput, actualOutput);
            } else {
                allPassed = false;
                log.debug("测试用例 {} 失败: 期望={}, 实际={}", index + 1, expectedOutput, actualOutput);
            }
        }

        return allPassed;
    }

    private void saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit();
        BeanUtil.copyProperties(userQuestionResultVO, userSubmit);
        userSubmit.setUserId(judgeSubmitDTO.getUserId());
        userSubmit.setQuestionId(judgeSubmitDTO.getQuestionId());
        userSubmit.setExamId(judgeSubmitDTO.getExamId());
        userSubmit.setProgramType(judgeSubmitDTO.getProgramType());
        userSubmit.setUserCode(judgeSubmitDTO.getUserCode());
        userSubmit.setCaseJudgeRes(JSON.toJSONString(userQuestionResultVO.getUserExeResultList()));
        // 手动设置审计字段
        Long userId = judgeSubmitDTO.getUserId();
        LocalDateTime now = LocalDateTime.now();
        userSubmit.setCreateBy(userId);
        userSubmit.setCreateTime(now);
        userSubmit.setUpdateBy(userId);
        userSubmit.setUpdateTime(now);

        // 删除旧的提交记录，保留最新的
        LambdaQueryWrapper<UserSubmit> wrapper = new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getQuestionId, judgeSubmitDTO.getQuestionId())
                .eq(UserSubmit::getUserId, judgeSubmitDTO.getUserId());

        if (judgeSubmitDTO.getExamId() != null) {
            wrapper.eq(UserSubmit::getExamId, judgeSubmitDTO.getExamId());
        } else {
            wrapper.isNull(UserSubmit::getExamId);
        }

        userSubmitMapper.delete(wrapper);
        userSubmitMapper.insert(userSubmit);

        log.info("保存提交记录成功: userId={}, questionId={}, score={}, pass={}",
                judgeSubmitDTO.getUserId(),
                judgeSubmitDTO.getQuestionId(),
                userQuestionResultVO.getScore(),
                userQuestionResultVO.getPass());
    }
}
