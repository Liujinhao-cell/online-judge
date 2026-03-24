package com.example.friend.domain.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 竞赛-题目关联表实体类
 *
 * @author example
 * @date 2026-3-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_exam_question")
public class ExamQuestion extends BaseEntity {
    /**
     * 竞赛题目关系id(主键)
     */
    @TableId(value = "EXAM_QUESTION_ID",type = IdType.ASSIGN_ID)
    private Long examQuestionId;

    /**
     * 题目id(主键)
     */
    private Long questionId;

    /**
     * 竞赛id(主键)
     */
    private Long examId;

    /**
     * 题目顺序
     */
    private Integer questionOrder;

}
