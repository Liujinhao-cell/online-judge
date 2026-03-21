package com.example.friend.domain.question.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Data;

@Data
public class QuestionQueryDTO extends PageQueryDTO {
    /**
     *关键字
     */
    private String keyword;

    /**
     * 难度
     */
    private Integer difficulty;
}
