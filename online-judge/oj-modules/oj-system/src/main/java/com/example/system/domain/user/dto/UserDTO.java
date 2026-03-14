package com.example.system.domain.user.dto;

import lombok.Data;

@Data
public class UserDTO {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户状态：0-拉黑 1-正常
     */
    private Integer status;
}
