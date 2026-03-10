package com.example.system.controller.question;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionDetailVO;
import com.example.system.service.question.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
@Tag(name = "题目管理接口")
public class QuestionController extends BaseController {
    @Autowired
    private QuestionService questionService;

    /**
     * 查询题目列表
     * @param questionQueryDTO 分页条件
     * @return {@link TableDataInfo }
     */
    @GetMapping("/list")
    @Operation(summary = "查询题目列表", description = "根据标题和难度查询题目列表")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO){
        return getTableDataInfo(questionService.list(questionQueryDTO));
    }

    /**
     * 添加题目
     * @param questionAddDTO 题目详细信息
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/add")
    @Operation(summary = "添加题目", description = "输入信息添加题目")
    public R<Void> add(@RequestBody @Validated QuestionAddDTO questionAddDTO){
        return toResult(questionService.add(questionAddDTO));
    }

    /**
     * 查询题目详情
     *
     * @param questionId 题目id
     * @return {@link R }<{@link QuestionDetailVO }>
     */
    @GetMapping("/detail")
    @Operation(summary = "题目详情", description = "查询题目详情")
    public R<QuestionDetailVO> detail(Long questionId){
        return R.ok(questionService.detail(questionId));
    }

    /**
     * 编辑题目
     *
     * @param questionEditDTO 题目信息
     * @return {@link R }<{@link Boolean }>
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑题目", description = "输入信息编辑题目")
    public R<Void> edit(@RequestBody @Validated QuestionEditDTO questionEditDTO){
         return toResult(questionService.edit(questionEditDTO));
    }

    /**
     * 删除题目
     * @param questionId 题目id
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除题目", description = "根据id删除题目")
    public R<Void> delete(Long questionId){
        return toResult(questionService.delete(questionId));
    }
}
