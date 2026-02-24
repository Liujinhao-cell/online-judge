package com.example.system.service;

import com.example.common.core.domain.R;
import com.example.system.controller.result.LoginResult;

public interface SysUserService {
    R<Void> login(String userAccount, String password);
}
