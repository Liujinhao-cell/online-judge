package com.example.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysUserSaveDTO {
    /**
     *帐号
     */
    @Schema(description = "用户帐号")
    private String userAccount;
    /**
     *密码
     */
    @Schema(description = "用户密码")
    private String password;
}
