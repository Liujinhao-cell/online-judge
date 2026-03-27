package com.example.job.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.job.domain.user.UserExam;
import com.example.job.domain.user.UserScore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserExamMapper extends BaseMapper<UserExam> {

    void updateUserScoreAndRank(List<UserScore> userScoreList);
}
