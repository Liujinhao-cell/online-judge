package com.example.system.domain.sysuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysUserVO {
    /**
     *帐号
     */
    @Schema(description = "用户帐号")
    private String userAccount;
    /**
     *密码
     */
    @Schema(description = "用户昵称")
    private String nickName;
}
