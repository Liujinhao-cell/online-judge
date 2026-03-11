package com.example.system.service.exam;

import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionAddDTO;
import com.example.system.domain.exam.vo.ExamVO;

import java.util.List;

public interface IExamService {
    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    int add(ExamAddDTO examAddDTO);

    boolean questionAdd(ExamQuestionAddDTO examQuestionAddDTO);
}
