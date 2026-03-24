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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
            return remoteJudgeService.dnJudgeJavaCode(judgeSubmitDTO);
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO submitDTO) {
        Long questionId = submitDTO.getQuestionId();
        //orElse查出返回数据本身 ，未查出返回null
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
        if(questionES != null){
            BeanUtil.copyProperties(questionES,judgeSubmitDTO);
        }else{
            Question question = questionMapper.selectById(questionId);
            BeanUtil.copyProperties(question,judgeSubmitDTO);
            questionES = new QuestionES();
            BeanUtil.copyProperties(question,questionES);
            questionRepository.save(questionES);
        }
        judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
        judgeSubmitDTO.setExamId(submitDTO.getExamId());
        judgeSubmitDTO.setProgramType(submitDTO.getProgramType());
        judgeSubmitDTO.setUserCode(codeConnect(submitDTO.getUserCode(),questionES.getMainFuc()));
        List<QuestionCase> questionCaseList = JSONUtil.toList(questionES.getQuestionCase(), QuestionCase.class);
        List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
        List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
        judgeSubmitDTO.setInputList(inputList);
        judgeSubmitDTO.setOutputList(outputList);
        return judgeSubmitDTO;
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
