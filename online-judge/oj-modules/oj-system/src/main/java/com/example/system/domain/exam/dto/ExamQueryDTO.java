package com.example.system.domain.exam.dto;

import com.example.common.core.domain.PageQueryDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ExamQueryDTO extends PageQueryDTO {
    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 竞赛开始时间
     */
    private String startTime;

    /**
     * 竞赛结束时间
     */
    private String endTime;

}
