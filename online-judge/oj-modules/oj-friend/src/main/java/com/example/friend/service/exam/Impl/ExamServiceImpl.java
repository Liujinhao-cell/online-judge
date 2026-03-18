package com.example.friend.service.exam.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.exam.dto.ExamQueryDTO;
import com.example.friend.domain.exam.vo.ExamVO;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.mapper.exam.ExamMapper;
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

    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        //redis中获取 之前竞赛列表的数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType(), 0L);
        List<ExamVO> examVOList = new ArrayList<>();
        //redis中无数据
        if(null == total || total <= 0){
            //数据库同步
            examVOList = list(examQueryDTO);
            examCacheManager.refreshCache(examQueryDTO.getType(),0L);
            total = new PageInfo<>(examVOList).getTotal();
        }else{
            examVOList = examCacheManager.getExamVOList(examQueryDTO,0L);
            total = examCacheManager.getListSize(examQueryDTO.getType(), 0L);
        }
        if(CollectionUtil.isEmpty(examVOList)){
            return TableDataInfo.empty();
        }
        return TableDataInfo.success(examVOList,total);
    }
}
