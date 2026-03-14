package com.example.friend.test;

import com.example.common.core.domain.R;
import com.example.common.message.util.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private MailUtil mailUtil;
    @GetMapping("/sendCode")
    public R<Void> sendCode(String email, String code){
        mailUtil.sendVerifyCode(email,code);
        return R.ok();
    }
}
