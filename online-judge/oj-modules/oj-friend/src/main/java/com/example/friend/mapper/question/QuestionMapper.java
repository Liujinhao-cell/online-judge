package com.example.friend.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.question.Question;
import com.example.friend.domain.question.vo.QuestionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    List<QuestionVO> selectHotQuestionList();
}
