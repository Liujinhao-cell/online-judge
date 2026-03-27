package com.example.friend.controller.question;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.question.dto.QuestionQueryDTO;
import com.example.friend.domain.question.vo.QuestionDetailVO;
import com.example.friend.domain.question.vo.QuestionVO;
import com.example.friend.mapper.question.QuestionMapper;
import com.example.friend.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {
    @Autowired
    private IQuestionService questionService;

    /**
     * 显示的题目列表
     * @param questionQueryDTO
     * @return {@link TableDataInfo }
     */
    @GetMapping("/semiLogin/list")
        public TableDataInfo list(QuestionQueryDTO questionQueryDTO){
            return questionService.list(questionQueryDTO);
    }

    /**
     * 答题的相关信息
     * @param questionId
     * @return {@link R }<{@link QuestionDetailVO }>
     */
    @GetMapping("/detail")
    public R<QuestionDetailVO> detail(Long questionId){
        return R.ok(questionService.detail(questionId));
    }

    /**
     * redis排序
     * @param questionId
     * @return {@link R }<{@link String }>
     */
    @GetMapping("/preQuestion")
    public R<String> preQuestion(Long questionId){
        return R.ok(questionService.preQuestion(questionId));
    }

    /**
     * redis排序
     * @param questionId
     * @return {@link R }<{@link String }>
     */
    @GetMapping("/nextQuestion")
    public R<String> nextQuestion(Long questionId){
        return R.ok(questionService.nextQuestion(questionId));
    }


    /**
     * 获取热榜题目前5
     */
    @GetMapping("/semiLogin/hotList")
    public R<List<QuestionVO>> hotList() {
        List<QuestionVO> hotList = questionService.getHotQuestionList();
        return R.ok(hotList);
    }
}
