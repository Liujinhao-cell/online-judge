package com.example.friend.service.question;

import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.question.dto.QuestionQueryDTO;
import com.example.friend.domain.question.vo.QuestionDetailVO;

public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);

    QuestionDetailVO detail(Long questionId);

    String preQuestion(Long questionId);

    String nextQuestion(Long questionId);
}
