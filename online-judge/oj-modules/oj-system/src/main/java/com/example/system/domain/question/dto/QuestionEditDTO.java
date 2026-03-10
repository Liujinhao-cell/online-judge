package com.example.system.domain.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Data
public class QuestionEditDTO {

    @NotNull(message = "题目ID不能为空")
    @JsonProperty("questionId")
    private Long questionId;

    @Length(min = 1, max = 200, message = "标题长度必须在1-200个字符之间")
    @JsonProperty("title")
    private String title;

    @Range(min = 1, max = 3, message = "难度等级必须是1(简单)、2(中等)或3(困难)")
    @JsonProperty("difficulty")
    private Integer difficulty;

    @Min(value = 100, message = "时间限制不能小于100ms")
    @Max(value = 10000, message = "时间限制不能大于10000ms")
    @JsonProperty("time_limit")
    private Long timeLimit;

    @Min(value = 16, message = "空间限制不能小于16MB")
    @Max(value = 1024, message = "空间限制不能大于1024MB")
    @JsonProperty("space_limit")
    private Long spaceLimit;

    @Length(min = 10, max = 65535, message = "题目内容长度必须在10-65535个字符之间")
    @JsonProperty("content")
    private String content;

    @Length(min = 1, max = 65535, message = "题目用例长度必须在1-65535个字符之间")
    @JsonProperty("question_case")
    private String questionCase;

    @Length(max = 65535, message = "默认代码长度不能超过65535个字符")
    @JsonProperty("default_code")
    private String defaultCode;

    @Length(max = 65535, message = "主函数长度不能超过65535个字符")
    @JsonProperty("main_fuc")
    private String mainFuc;
}