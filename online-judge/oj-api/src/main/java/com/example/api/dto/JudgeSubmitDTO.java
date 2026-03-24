package com.example.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class JudgeSubmitDTO {

    private Long userId;

    private Long examId;

    //编程语言类型 (0 java 1 C++)
    private Integer programType;

    private Long questionId;

    //题目难度
    private Integer difficulty;

    //时间限制 ms
    private Long timeLimit;

    //空间限制 kb
    private Long spaceLimit;

    //用户提交的代码+main函数
    private String userCode;

    //题目用例
    private List<String> inputList;

    //题型输出的数据
    private List<String> outputList;
}
