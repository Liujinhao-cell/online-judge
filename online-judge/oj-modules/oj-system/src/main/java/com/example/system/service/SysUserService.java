package com.example.system.service;

import com.example.common.core.domain.R;
import com.example.system.controller.result.LoginResult;
import com.example.system.domain.SysUserSaveDTO;

public interface SysUserService {
    R<String> login(String userAccount, String password);

    int add(SysUserSaveDTO sysUserSaveDTO);
}
