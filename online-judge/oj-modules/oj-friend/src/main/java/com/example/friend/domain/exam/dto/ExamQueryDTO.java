package com.example.friend.domain.exam.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Data;

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

    /**
     *  0 未完赛 1 历史
     */
    private Integer type;
}
