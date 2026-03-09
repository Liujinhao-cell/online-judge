package com.example.system.domain.sysuser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("tb_sys_user")
public class SysUser extends BaseEntity {
    //主键自动生成（雪花算法）
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private String userAccount;

    private String password;

    private String nickName;

}
