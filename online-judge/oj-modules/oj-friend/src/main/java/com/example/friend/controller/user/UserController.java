package com.example.friend.controller.user;

import com.example.common.core.constants.HttpConstants;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.friend.domain.user.dto.UserDTO;
import com.example.friend.domain.user.dto.UserUpdateDTO;
import com.example.friend.domain.user.vo.UserVO;
import com.example.friend.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/code/login")
    @Operation(summary = "登录/注册", description = "根据QQ邮箱发送验证码")
    public R<String> codeLogin(@RequestBody UserDTO userDTO){
        return R.ok(userService.codeLogin(userDTO.getEmail(),userDTO.getCode()));
    }

    /**
     * 退出登录
     * @param token JWT令牌
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return toResult(userService.logout(token));
    }
    /**
     * @param token
     * @return {@link R }<{@link String }>
     */
    @GetMapping("/info")
    @Operation(summary = "用户昵称", description = "根据token来得到用户昵称和头像")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return userService.info(token);
    }

    /**
     *
     * @return {@link R }<{@link UserVO }>
     */
    @GetMapping("/detail")
    @Operation(summary = "用户详细信息", description = "根据token来获取用户详细信息")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    public R<UserVO> detail(){
        return R.ok(userService.detail());
    }

    @PutMapping("/edit")
    @Operation(summary = "编辑用户信息", description = "根据传入的数据更新用户信息")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    public R<Void> edit(@RequestBody UserUpdateDTO userUpdateDTO){
        return toResult(userService.edit(userUpdateDTO));
    }

    @PutMapping("/head-image/update")
    @Operation(summary = "头像更新", description = "根据返回的标识确定图片位置")
    public R<Void> updateHeadImage(@RequestBody UserUpdateDTO userUpdateDTO){
        return toResult(userService.updateHeadImage(userUpdateDTO.getHeadImage()));
    }
}
