package com.example.common.security.domain;

import lombok.Data;

@Data
public class LoginUser {
    /**
     * identity 1:普通用户 2:管理员
     */
    private Integer identity;

}
