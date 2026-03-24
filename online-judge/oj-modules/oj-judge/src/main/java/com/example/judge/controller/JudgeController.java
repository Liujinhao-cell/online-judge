package com.example.judge.controller;

import com.example.api.dto.JudgeSubmitDTO;
import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;

import com.example.judge.service.IJudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/judge")
public class JudgeController extends BaseController {
    @Autowired
    private IJudgeService judgeService;
    @PostMapping("/doJudgeJavaCode")
    R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO){
        return R.ok(judgeService.doJudgeJavaCode(judgeSubmitDTO));
    }
}
