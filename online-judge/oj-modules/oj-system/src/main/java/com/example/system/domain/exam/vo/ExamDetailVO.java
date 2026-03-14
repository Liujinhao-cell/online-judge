package com.example.system.domain.exam.vo;

import com.example.system.domain.question.vo.QuestionVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamDetailVO {
    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 竞赛开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 竞赛结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     *题目信息列表
     */
    private List<QuestionVO> examQuestionList;


}
