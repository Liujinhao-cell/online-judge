package com.example.friend.service.exam.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.TableDataInfo;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.domain.exam.dto.ExamQueryDTO;
import com.example.friend.domain.exam.dto.ExamRankDTO;
import com.example.friend.domain.exam.vo.ExamRankVO;
import com.example.friend.domain.exam.vo.ExamVO;
import com.example.friend.domain.user.vo.UserVO;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.manager.UserCacheManager;
import com.example.friend.mapper.exam.ExamMapper;
import com.example.friend.mapper.user.UserExamMapper;
import com.example.friend.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private ExamCacheManager examCacheManager;
    @Autowired
    private UserExamMapper userExamMapper;
    @Autowired
    private UserCacheManager userCacheManager;
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        //redis中获取 之前竞赛列表的数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        List<ExamVO> examVOList = new ArrayList<>();
        //redis中无数据
        if(null == total || total <= 0){
            //数据库同步
            examVOList = list(examQueryDTO);
            examCacheManager.refreshCache(examQueryDTO.getType(),null);
            total = new PageInfo<>(examVOList).getTotal();
        }else{
            examVOList = examCacheManager.getExamVOList(examQueryDTO,null);
            total = examCacheManager.getListSize(examQueryDTO.getType(), null);
        }
        if(CollectionUtil.isEmpty(examVOList)){
            return TableDataInfo.empty();
        }
        assembleExamVOList(examVOList);
        return TableDataInfo.success(examVOList,total);
    }

    @Override
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        //redis中获取 之前竞赛列表的数据
        Long total = examCacheManager.getRankListSize(examRankDTO.getExamId());
        List<ExamRankVO> examRankVOList = new ArrayList<>();
        //redis中无数据
        if(null == total || total <= 0){
            //数据库同步
            PageHelper.startPage(examRankDTO.getPageNum(),examRankDTO.getPageSize());
            examRankVOList = userExamMapper.selectExamRankList(examRankDTO.getExamId());
            examCacheManager.refreshExamRankCache(examRankDTO.getExamId());
            total = new PageInfo<>(examRankVOList).getTotal();
        }else{
            examRankVOList = examCacheManager.getExamRankList(examRankDTO);
        }
        if(CollectionUtil.isEmpty(examRankVOList)){
            return TableDataInfo.empty();
        }
        assembleExamRankVOList(examRankVOList);
        return TableDataInfo.success(examRankVOList,total);
    }

    @Override
    public String getFirstQuestion(Long examId) {
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if(null == listSize || listSize <= 0){
            examCacheManager.refreshExamQuestionCache(examId);
        }
        return examCacheManager.getFirstQuestion(examId).toString();
    }

    @Override
    public String preQuestion(Long examId, Long questionId) {
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if(null == listSize || listSize <= 0){
            examCacheManager.refreshExamQuestionCache(examId);
        }
        return examCacheManager.preQuestion(examId,questionId).toString();
    }

    @Override
    public String nextQuestion(Long examId,Long questionId) {
        Long listSize = examCacheManager.getExamQuestionListSize(examId);
        if(null == listSize || listSize <= 0){
            examCacheManager.refreshExamQuestionCache(examId);
        }
        return examCacheManager.nextQuestion(examId,questionId).toString();
    }

    private void assembleExamVOList(List<ExamVO> examVOList) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        List<Long> userExamIdList = examCacheManager.getAllUserExamList(userId);
        if(CollectionUtil.isEmpty(userExamIdList)){
            return;
        }
        for(ExamVO examVO:examVOList){
            if(userExamIdList.contains(examVO.getExamId())){
                examVO.setEnter(true);
            }
        }
    }

    private void assembleExamRankVOList(List<ExamRankVO> examRankVOList) {
        if(CollectionUtil.isEmpty(examRankVOList)){
            return;
        }
        for(ExamRankVO examRankVO:examRankVOList){
            Long userId = examRankVO.getUserId();
            UserVO userVO = userCacheManager.getUserId(userId);
            examRankVO.setNickName(userVO.getNickName());
        }
    }
}
