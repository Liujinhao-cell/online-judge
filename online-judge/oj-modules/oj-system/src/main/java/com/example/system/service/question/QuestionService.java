package com.example.system.service.question;

import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionDetailVO;
import com.example.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface QuestionService {
    List<QuestionVO> list(QuestionQueryDTO questionQueryDTO);

    boolean add(QuestionAddDTO questionAddDTO);

    QuestionDetailVO detail(Long questionId);

    int edit(QuestionEditDTO questionEditDTO);

    int delete(Long questionId);
}
