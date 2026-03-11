package com.example.system.domain.exam.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ExamQuestionAddDTO {

    /**
     * 竞赛id
     */
    private Long examId;

    /**
     * 题目id列表
     */
    private Set<Long> questionIdSet;


}
