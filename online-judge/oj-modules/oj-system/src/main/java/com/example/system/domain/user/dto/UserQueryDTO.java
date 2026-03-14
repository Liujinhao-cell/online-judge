package com.example.system.domain.user.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Data;

@Data
public class UserQueryDTO extends PageQueryDTO {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String nickName;
}
