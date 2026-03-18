package com.example.friend.service.user;

import com.example.friend.domain.exam.dto.ExamDTO;

public interface IUserExamService {
    int enter(String token, Long examId);
}
