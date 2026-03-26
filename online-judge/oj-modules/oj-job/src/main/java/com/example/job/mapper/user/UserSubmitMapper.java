package com.example.job.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.job.domain.user.UserScore;
import com.example.job.domain.user.UserSubmit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserSubmitMapper extends BaseMapper<UserSubmit> {

    List<UserScore> selectUserScoreList(Set<Long> examIdSet);
}
