package com.example.friend.controller.user;

import com.example.api.vo.UserQuestionResultVO;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.friend.domain.user.dto.UserSubmitDTO;
import com.example.friend.service.user.IUserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/question")
public class UserQuestionController extends BaseController {
    @Autowired
    private IUserQuestionService userQuestionService;
    @PostMapping("/submit")
    public R<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO userSubmitDTO){
        return userQuestionService.submit(userSubmitDTO);
    }
}
