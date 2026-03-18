package com.example.friend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.exam.vo.ExamVO;
import com.example.friend.domain.user.UserExam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserExamMapper extends BaseMapper<UserExam> {
    List<ExamVO> selectUserExamList(Long userId);
}
