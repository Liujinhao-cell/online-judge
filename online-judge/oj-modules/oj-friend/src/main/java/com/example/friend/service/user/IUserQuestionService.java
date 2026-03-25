package com.example.friend.service.user;

import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.domain.R;
import com.example.friend.domain.user.dto.UserSubmitDTO;

public interface IUserQuestionService {
    R<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO);

    boolean rabbitSubmit(UserSubmitDTO userSubmitDTO);

    UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime);
}
