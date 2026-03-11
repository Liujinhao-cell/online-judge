package com.example.system.controller.exam;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionAddDTO;
import com.example.system.service.exam.IExamService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {
    @Autowired
    private IExamService examService;
    /**
     * 查询竞赛列表
     * @param examQueryDTO 竞赛列表页码信息
     * @return {@link TableDataInfo }
     */
    @GetMapping("/list")
    @Operation(summary = "竞赛列表", description = "查询竞赛列表")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    /**
     * 新增不包含题目的竞赛
     * @param examAddDTO 竞赛信息
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/add")
    @Operation(summary = "新增不包含题目的竞赛", description = "根据数据新增不包含题目的竞赛")
    public R<Void> add(@RequestBody ExamAddDTO examAddDTO){
        return toResult(examService.add(examAddDTO));
    }

    /**
     * 新增包含题目的竞赛
     * @param examQuestionAddDTO 题目信息
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/question/add")
    @Operation(summary = "新增不包含题目的竞赛", description = "根据数据新增不包含题目的竞赛")
    public R<Void> questionAdd(@RequestBody ExamQuestionAddDTO examQuestionAddDTO){
        return toResult(examService.questionAdd(examQuestionAddDTO));
    }
}
