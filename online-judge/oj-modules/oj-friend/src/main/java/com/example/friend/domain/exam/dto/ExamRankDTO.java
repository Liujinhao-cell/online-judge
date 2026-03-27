package com.example.friend.domain.exam.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Data;

@Data
public class ExamRankDTO extends PageQueryDTO {
    private Long examId;

}
