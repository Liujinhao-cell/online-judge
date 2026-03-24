package com.example.friend.domain.user.dto;

import lombok.Data;

@Data
public class UserSubmitDTO {
    /**
     *可选参数
     */
    private Long examId;

    private Long questionId;
    /**
     * 语言类型 0 java 1 cpp 2 python
     */
    private Integer programType;

    private String userCode;
}
