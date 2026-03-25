package com.example.friend.service.user.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.example.api.RemoteJudgeService;
import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.R;
import com.example.common.core.enums.ProgramType;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.question.Question;
import com.example.friend.domain.question.QuestionCase;
import com.example.friend.domain.question.es.QuestionES;
import com.example.friend.domain.user.dto.UserSubmitDTO;
import com.example.friend.mapper.question.QuestionMapper;
import com.example.friend.mapper.question.QuestionRepository;
import com.example.friend.service.user.IUserQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserQuestionServiceImpl implements IUserQuestionService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private RemoteJudgeService remoteJudgeService;
    @Override
    public R<UserQuestionResultVO> submit(UserSubmitDTO submitDTO) {
        Integer programType = submitDTO.getProgramType();
        if(ProgramType.JAVA.getValue().equals(programType)){
            //java
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(submitDTO);
            return remoteJudgeService.doJudgeJavaCode(judgeSubmitDTO);
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

//    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO submitDTO) {
//        Long questionId = submitDTO.getQuestionId();
//        //orElse查出返回数据本身 ，未查出返回null
//        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
//        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
//        if(questionES != null){
//            BeanUtil.copyProperties(questionES,judgeSubmitDTO);
//        }else{
//            Question question = questionMapper.selectById(questionId);
//            BeanUtil.copyProperties(question,judgeSubmitDTO);
//            questionES = new QuestionES();
//            BeanUtil.copyProperties(question,questionES);
//            questionRepository.save(questionES);
//        }
//        judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
//        judgeSubmitDTO.setExamId(submitDTO.getExamId());
//        judgeSubmitDTO.setProgramType(submitDTO.getProgramType());
//        judgeSubmitDTO.setUserCode(codeConnect(submitDTO.getUserCode(),questionES.getMainFuc()));
//        List<QuestionCase> questionCaseList = JSONUtil.toList(questionES.getQuestionCase(), QuestionCase.class);
//        List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
//        List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
//        judgeSubmitDTO.setInputList(inputList);
//        judgeSubmitDTO.setOutputList(outputList);
//        return judgeSubmitDTO;
//    }
private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO submitDTO) {
    Long questionId = submitDTO.getQuestionId();
    QuestionES questionES = questionRepository.findById(questionId).orElse(null);
    JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();

    if (questionES != null) {
        BeanUtil.copyProperties(questionES, judgeSubmitDTO);
    } else {
        Question question = questionMapper.selectById(questionId);
        BeanUtil.copyProperties(question, judgeSubmitDTO);
        questionES = new QuestionES();
        BeanUtil.copyProperties(question, questionES);
        questionRepository.save(questionES);
    }

    judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
    judgeSubmitDTO.setExamId(submitDTO.getExamId());
    judgeSubmitDTO.setProgramType(submitDTO.getProgramType());
    judgeSubmitDTO.setUserCode(codeConnect(submitDTO.getUserCode(), questionES.getMainFuc()));

    // ========== 修改这里：使用安全的解析方法 ==========
    List<QuestionCase> questionCaseList = parseQuestionCaseSafely(questionES.getQuestionCase());
    // ===============================================

    List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
    List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
    judgeSubmitDTO.setInputList(inputList);
    judgeSubmitDTO.setOutputList(outputList);

    return judgeSubmitDTO;
}

    /**
     * 安全解析测试用例，兼容多种格式
     */
    private List<QuestionCase> parseQuestionCaseSafely(String questionCaseStr) {
        List<QuestionCase> result = new ArrayList<>();

        if (questionCaseStr == null || questionCaseStr.trim().isEmpty()) {
            log.warn("测试用例为空");
            return result;
        }

        String trimmed = questionCaseStr.trim();

        // 1. 尝试解析为 JSON 数组格式
        if (trimmed.startsWith("[")) {
            try {
                return JSONUtil.toList(trimmed, QuestionCase.class);
            } catch (Exception e) {
                log.warn("JSON解析失败: {}", e.getMessage());
            }
        }

        // 2. 尝试解析自定义格式（输入：xxx 输出：xxx）
        result = parseCustomFormat(trimmed);
        if (!result.isEmpty()) {
            return result;
        }

        // 3. 尝试解析单行格式
        QuestionCase singleCase = parseSingleTestCase(trimmed);
        if (singleCase != null) {
            result.add(singleCase);
            return result;
        }

        log.error("无法解析测试用例格式: {}", trimmed);
        return result;
    }

    /**
     * 解析自定义多行格式
     */
    private List<QuestionCase> parseCustomFormat(String text) {
        List<QuestionCase> result = new ArrayList<>();
        String[] lines = text.split("\n");
        QuestionCase currentCase = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("解释")) {
                continue;
            }

            if (line.startsWith("输入：")) {
                currentCase = new QuestionCase();
                String input = line.substring(3).trim();
                currentCase.setInput(input);
            } else if (line.startsWith("输出：") && currentCase != null) {
                String output = line.substring(3).trim();
                if (output.endsWith("。")) {
                    output = output.substring(0, output.length() - 1);
                }
                currentCase.setOutput(output);
                result.add(currentCase);
                currentCase = null;
            }
        }

        return result;
    }

    /**
     * 解析单行格式：输入：xxx 输出：xxx
     */
    private QuestionCase parseSingleTestCase(String line) {
        if (line.contains("输入：") && line.contains("输出：")) {
            try {
                int inputIdx = line.indexOf("输入：");
                int outputIdx = line.indexOf("输出：");

                if (inputIdx != -1 && outputIdx != -1 && outputIdx > inputIdx) {
                    QuestionCase testCase = new QuestionCase();
                    String input = line.substring(inputIdx + 3, outputIdx).trim();
                    String output = line.substring(outputIdx + 3).trim();

                    if (output.contains("。")) {
                        output = output.substring(0, output.indexOf("。"));
                    }

                    testCase.setInput(input);
                    testCase.setOutput(output);
                    return testCase;
                }
            } catch (Exception e) {
                log.warn("解析单行测试用例失败: {}", line, e);
            }
        }
        return null;
    }

    private String codeConnect(String userCode, String mainFunc) {
        // 1. 参数校验
        if (userCode == null || userCode.trim().isEmpty()) {
            throw new ServiceException(ResultCode.FAILED, "用户代码不能为空");
        }
        if (mainFunc == null || mainFunc.trim().isEmpty()) {
            throw new ServiceException(ResultCode.FAILED, "主函数代码不能为空");
        }

        String targetCharacter = "}";
        int targetLastIndex = userCode.lastIndexOf(targetCharacter);

        if (targetLastIndex != -1) {
            // 2. 考虑插入位置的美观性
            String before = userCode.substring(0, targetLastIndex);
            String after = userCode.substring(targetLastIndex);

            // 3. 处理换行符，使生成的代码格式更整洁
            if (!before.endsWith("\n")) {
                before += "\n";
            }
            if (!after.startsWith("\n")) {
                after = "\n" + after;
            }

            return before + mainFunc + after;
        }

        // 4. 更详细的错误信息
        throw new ServiceException(ResultCode.FAILED,
                "无法找到插入位置：用户代码中未找到 '}' 字符");
    }
}
