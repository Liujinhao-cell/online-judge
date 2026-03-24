package com.example.judge.service;

import com.example.judge.domain.SandBoxExecuteResult;

import java.util.List;

public interface ISandboxService {
    SandBoxExecuteResult exeJavaCode(String userCode, List<String> inputList);
}
