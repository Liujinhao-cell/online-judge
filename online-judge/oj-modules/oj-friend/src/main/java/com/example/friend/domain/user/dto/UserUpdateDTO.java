package com.example.friend.domain.user.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    /**
     * 用户头像
     */
    private String headImage;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 性别：1-男 2-女
     */
    private Integer sex;

    /**
     * 手机号
     */
    private String phone = "";

    /**
     * 邮箱
     */
    private String email;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 学校
     */
    private String schoolName;

    /**
     * 专业
     */
    private String majorName;

    /**
     * 个人介绍
     */
    private String introduce;

}
