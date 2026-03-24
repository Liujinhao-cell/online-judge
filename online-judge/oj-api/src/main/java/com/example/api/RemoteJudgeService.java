package com.example.api;

import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "RemoteJudgeService",value = Constants.JUDGE_SERVICE)
public interface RemoteJudgeService {
    @PostMapping("/judge/doJudgeJavaCode")
    R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO);
}
