package com.example.system.domain.user.vo;

import lombok.Data;

@Data
public class UserVO {
    /**
     * 用户id
     */
    private Long userId;
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
    private String phone;

    /**
     * 验证码
     */
    private String code;

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

    /**
     * 用户状态：0-拉黑 1-正常
     */
    private Integer status;
}
