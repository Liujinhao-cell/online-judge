package com.example.system.mapper.exam;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.system.domain.exam.Exam;
import com.example.system.domain.exam.ExamQuestion;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.vo.ExamVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

}
