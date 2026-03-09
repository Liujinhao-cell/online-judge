package com.example.system.service.sysuser;

import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.system.domain.sysuser.SysUserSaveDTO;

public interface SysUserService {
    R<String> login(String userAccount, String password);
    boolean logout(String token);
    R<LoginUserVO> info(String token);
    int add(SysUserSaveDTO sysUserSaveDTO);

}
