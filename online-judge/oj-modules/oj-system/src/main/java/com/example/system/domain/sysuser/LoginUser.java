package com.example.system.domain.sysuser;

import lombok.Data;

@Data
public class LoginUser {
    /**
     * identity 1:普通用户 2:管理员
     */
    private Integer identity;

}
