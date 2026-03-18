package com.example.friend.domain.user.dto;

import lombok.Data;

@Data
public class UserDTO {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String code;
}
/**
 *手机号
 */
//    private String phone;
