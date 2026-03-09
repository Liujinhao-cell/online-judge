package com.example.system.service.question;

import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface QuestionService {
    List<QuestionVO> list(QuestionQueryDTO questionQueryDTO);
}
