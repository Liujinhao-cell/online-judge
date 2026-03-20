package com.example.friend.controller.user;

import com.example.common.core.constants.HttpConstants;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.friend.domain.exam.dto.ExamDTO;
import com.example.friend.domain.exam.dto.ExamQueryDTO;
import com.example.friend.service.user.IUserExamService;
import com.example.friend.service.user.Impl.UserExamServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController{
    @Autowired
    private IUserExamService userExamService;
    @PostMapping("/enter")
    @Operation(summary = "用户报名竞赛", description = "根据用户id用户报名竞赛")
    public R<Void> enter(@RequestHeader(HttpConstants.AUTHENTICATION) String token,@RequestBody ExamDTO examDTO){
        return toResult(userExamService.enter(token,examDTO.getExamId()));
    }

    @GetMapping("/list")
    @Operation(summary = "我的竞赛列表", description = "查询我的竞赛列表")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return userExamService.list(examQueryDTO);
    }
}
