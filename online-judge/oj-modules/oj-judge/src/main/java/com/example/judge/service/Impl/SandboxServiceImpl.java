package com.example.judge.service.Impl;

import com.example.judge.domain.SandBoxExecuteResult;
import com.example.judge.service.ISandboxService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SandboxServiceImpl implements ISandboxService {

    @Override
    public SandBoxExecuteResult exeJavaCode(String userCode, List<String> inputList) {
        return null;
    }
}
