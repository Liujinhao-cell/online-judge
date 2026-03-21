package com.example.friend.service.question;

import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.question.dto.QuestionQueryDTO;

public interface IQuestionService {
    TableDataInfo list(QuestionQueryDTO questionQueryDTO);
}
