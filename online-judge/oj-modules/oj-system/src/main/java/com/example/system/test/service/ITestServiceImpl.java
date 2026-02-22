package com.example.system.test.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.system.test.mapper.TestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ITestServiceImpl implements ITestService{
    @Autowired
    private TestMapper testMapper;
    @Override
    public List<?> list() {
        return testMapper.selectList(new LambdaQueryWrapper<>());
    }
}
