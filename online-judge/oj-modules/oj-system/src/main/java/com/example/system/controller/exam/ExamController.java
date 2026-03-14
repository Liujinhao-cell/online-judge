package com.example.system.controller.exam;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamEditDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionAddDTO;
import com.example.system.domain.exam.vo.ExamDetailVO;
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

    /**
     * 删除竞赛中题目
     * @param examId 竞赛Id
     * @param questionId 题目Id
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/question/delete")
    @Operation(summary = "删除竞赛中题目", description = "根据questionId删除竞赛中题目")
    public R<Void> questionDelete(Long examId,Long questionId){
        return toResult(examService.questionDelete(examId,questionId));
    }
    /**
     * 查询竞赛详情
     * @param examId 竞赛Id
     * @return {@link R }<{@link ExamDetailVO }>
     */
    @GetMapping("/detail")
    @Operation(summary = "竞赛详情", description = "根据questionId查询竞赛详情")
    public R<ExamDetailVO> detail(Long examId){
        ExamDetailVO detail = examService.detail(examId);
        return R.ok(detail);
    }

    /**
     * 编辑竞赛
     * @param examEditDTO 竞赛数据
     * @return {@link R }<{@link Void }>
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑竞赛", description = "根据数据编辑竞赛")
    public R<Void> edit(@RequestBody ExamEditDTO examEditDTO){
        return toResult(examService.edit(examEditDTO));
    }

    /**
     * 删除竞赛
     * @param examId 竞赛Id
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除竞赛", description = "根据竞赛Id删除竞赛数据")
    public R<Void> delete(Long examId){
        return toResult(examService.detele(examId));
    }

    /**
     * 竞赛发布
     * @param examId 竞赛Id
     * @return {@link R }<{@link Void }>
     */
    @PutMapping("/publish")
    @Operation(summary = "竞赛发布", description = "根据竞赛Id发布竞赛数据")
    public R<Void> publish(Long examId){
        return toResult(examService.publish(examId));
    }

    @PutMapping("/cancelPublish")
    @Operation(summary = "竞赛撤销发布", description = "根据竞赛Id撤销发布")
    public R<Void> cancelPublish(Long examId){
        return toResult(examService.cancelPublish(examId));
    }
}
