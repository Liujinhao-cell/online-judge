package com.example.friend.domain.exam.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamVO {
    /**
     * 竞赛id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long examId;
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
     *报名情况 true 已报名 false 未报名
     */
    private boolean enter;
}
