package com.example.system.controller.sysuser;

import com.example.common.core.constants.HttpConstants;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.R;
import com.example.common.core.domain.vo.LoginUserVO;
import com.example.system.domain.sysuser.LoginDTO;
import com.example.system.domain.sysuser.SysUserSaveDTO;
import com.example.system.domain.sysuser.SysUserVO;
import com.example.system.service.sysuser.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sysUser")
@Tag(name = "管理员接口")
public class SysUserController extends BaseController {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     *
     * @param loginDTO
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "根据帐号密码管理员进行登录")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    @ApiResponse(responseCode = "3102", description = "⽤⼾不存在")
    @ApiResponse(responseCode = "3103", description = "⽤⼾名或密码错误")
    public R<String> login(@RequestBody LoginDTO loginDTO) {
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    /**
     * 退出登录
     * @param token JWT令牌
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
       return toResult(sysUserService.logout(token));
    }
    /**
     * @param token
     * @return {@link R }<{@link String }>
     */
    @GetMapping("/info")
    @Operation(summary = "管理员昵称", description = "根据token来得到管理员昵称")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return sysUserService.info(token);
    }
    /**
     * 管理员增删改查
     * 新增管理员
     * @param sysUserSaveDTO
     * @return {@link R }<{@link Void }>
     */
    @PostMapping("/add")
    @Operation(summary = "新增管理员", description = "根据提供的信息新增管理员")
    @ApiResponse(responseCode = "1000", description = "操作成功")
    @ApiResponse(responseCode = "2000", description = "服务器繁忙，请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤⼾已存在")
    public R<Void> add(@RequestBody SysUserSaveDTO sysUserSaveDTO) {
        return toResult(sysUserService.add(sysUserSaveDTO));
    }

    /**
     * @param userId
     * @return {@link R }<{@link Void }>
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除⽤户", description = "通过⽤⼾id删除⽤户")
    @Parameters(value = {@Parameter(name = "userId", in = ParameterIn.PATH, description = "⽤户ID")})
    @ApiResponse(responseCode = "1000", description = "成功删除⽤户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤⼾不存在")
    public R<Void> delete(@PathVariable Long userId) {
        return null;
    }

    /**
     * @param userId
     * @param sex
     * @return {@link R }<{@link SysUserVO }>
     */
    @Operation(summary = "⽤户详情", description = "根据查询条件查询⽤⼾详情")
    @GetMapping("/detail")
    @Parameters(value = {@Parameter(name = "userId", in = ParameterIn.QUERY, description = "⽤⼾ID"),
            @Parameter(name = "sex", in = ParameterIn.QUERY, description = "⽤⼾性别")})
    @ApiResponse(responseCode = "1000", description = "成功获取⽤⼾信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤⼾不存在")
    public R<SysUserVO> detail(@RequestParam(required = true) Long userId, @RequestParam(required = false) String sex) {
        return null;
    }
}
