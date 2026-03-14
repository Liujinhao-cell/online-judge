package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.friend.domain.dto.UserDTO;
import com.example.friend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;

    @PostMapping("/sendCode")
    @Operation(summary = "发送验证码", description = "根据QQ邮箱发送验证码")
    public R<Void> sendCode(@RequestBody UserDTO userDTO){
        userService.sendCode(userDTO);
        return R.ok();
    }
}
