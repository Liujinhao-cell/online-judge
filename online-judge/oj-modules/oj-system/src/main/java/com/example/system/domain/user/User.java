package com.example.system.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
@TableName("tb_user")
public class User extends BaseEntity {
    /**
     * 用户id（主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "USER_ID",type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String headImage;

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
