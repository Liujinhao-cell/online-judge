package com.example.system.domain.question;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@TableName("tb_question")
@Data
public class Question extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long questionId;

    private String title;

    private Integer difficulty;

    @TableField("time_limit")
    private Long timeLimit;

    @TableField("space_limit")
    private Long spaceLimit;

    private String content;

    @TableField("question_case")
    private String questionCase;

    @TableField("default_code")
    private String defaultCode;

    @TableField("main_fuc")
    private String mainFuc;
}

