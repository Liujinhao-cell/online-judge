package com.example.system.domain.question.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionQueryDTO extends PageQueryDTO {
    /**
     * 标题
     */
    private String title;

    /**
     *难度
     */
    private Integer difficulty;
    /**
     * 需要排除的ID集合（用于排除已选择的题目等场景）
     */
    private Set<Long> excludeIdSet;
}
