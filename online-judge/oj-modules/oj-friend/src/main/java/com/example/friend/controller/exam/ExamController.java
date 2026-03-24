package com.example.friend.controller.exam;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.exam.dto.ExamQueryDTO;
import com.example.friend.service.exam.IExamService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {
    @Autowired
    private IExamService examService;

    @GetMapping("/semiLogin/list")
    @Operation(summary = "竞赛列表", description = "查询竞赛列表")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @GetMapping("/semiLogin/redis/list")
    @Operation(summary = "竞赛列表", description = "redis中查询竞赛列表")
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO){
        return examService.redisList(examQueryDTO);
    }

    @GetMapping("/getFirstQuestion")
    public R<String> getFirstQuestion(Long examId){
        return R.ok(examService.getFirstQuestion(examId));
    }
    @GetMapping("/preQuestion")
    public R<String> preQuestion(Long examId,Long questionId){
        return R.ok(examService.preQuestion(questionId,examId));
    }

    @GetMapping("/nextQuestion")
    public R<String> nextQuestion(Long examId,Long questionId){
        return R.ok(examService.nextQuestion(examId,questionId));
    }
    
}
