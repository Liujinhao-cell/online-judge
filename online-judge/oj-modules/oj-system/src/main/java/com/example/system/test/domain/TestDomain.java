package com.example.system.test.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("tb_test")
@Data
public class TestDomain {
    @TableId(type = IdType.ASSIGN_ID)
    private Long testId;
    private String title;
    private String content;

}
