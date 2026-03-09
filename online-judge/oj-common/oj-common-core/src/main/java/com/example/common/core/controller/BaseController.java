package com.example.common.core.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Component;

import java.util.List;

public class BaseController {
    public R<Void> toResult(int rows){
        return rows > 0 ? R.ok():R.fail();
    }

    public R<Void> toResult(boolean result){
        return result ? R.ok():R.fail();
    }

    public TableDataInfo getTableDataInfo(List<?> questionVOList){
        if(CollectionUtil.isEmpty(questionVOList)){
            return TableDataInfo.empty();
        }
        //总数
        long total = new PageInfo<>(questionVOList).getTotal();
        return TableDataInfo.success(questionVOList,total);
    }
}
