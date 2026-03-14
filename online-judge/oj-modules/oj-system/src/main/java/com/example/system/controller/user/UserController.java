package com.example.system.controller.user;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.TableDataInfo;
import com.example.system.domain.user.dto.UserDTO;
import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    @Autowired
    private IUserService userService;
    /**
     * 查询用户列表
     * @param userQueryDTO 页码信息
     * @return {@link TableDataInfo }
     */
    @GetMapping("/list")
    @Operation(summary = "查询用户列表", description = "查询用户列表")
    public TableDataInfo list(UserQueryDTO userQueryDTO){
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    @PutMapping("/updateStatus")
    //todo 限制:拉黑限制用户操作 解禁：用户操作解放
    //操作数据库
    @Operation(summary = "查询用户列表", description = "查询用户列表")
    public R<Void> updateStatus(@RequestBody UserDTO userDTO){
         return toResult(userService.updateStatus(userDTO));
    }
}
