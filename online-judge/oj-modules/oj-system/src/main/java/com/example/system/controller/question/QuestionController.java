package com.example.system.controller.question;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.service.question.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
@Tag(name = "题目管理接口")
public class QuestionController extends BaseController {
    @Autowired
    private QuestionService questionService;
    @GetMapping("/list")
    @Operation(summary = "查询题目列表", description = "根据标题和难度查询题目列表")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO){
        return getTableDataInfo(questionService.list(questionQueryDTO));
    }

}
