package com.example.system.test.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.system.test.domain.TestDomain;
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

    @Override
    public String add() {
        TestDomain testDomain = new TestDomain();
        testDomain.setTitle("测试");
        testDomain.setContent("测试UUID生成");
        testMapper.insert(testDomain);
        return "添加数据成功";
    }
}
