package com.example.api.vo;

import com.example.api.UserExeResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class UserQuestionResultVO {
    /**
     *是否通过标识
     */
    private Integer pass;
    /**
     *异常信息
     */
    private String exeMessage;
    /**
     * 测试用例列表
     */
    private List<UserExeResult> userExeResultList;

    @JsonIgnore
    private Integer score;
}
