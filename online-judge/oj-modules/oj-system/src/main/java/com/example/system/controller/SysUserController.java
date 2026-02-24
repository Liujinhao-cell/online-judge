package com.example.system.controller;

import com.example.common.core.domain.R;
import com.example.system.controller.result.LoginResult;
import com.example.system.domain.LoginDTO;
import com.example.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     *
     * @param loginDTO
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/login")
    public R<Void> login(@RequestBody LoginDTO loginDTO){
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }
}
