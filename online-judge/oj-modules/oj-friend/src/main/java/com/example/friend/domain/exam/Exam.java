package com.example.friend.domain.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 竞赛信息表实体类
 *
 * @author example
 * @date 2026-03-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_exam")
public class Exam extends BaseEntity {
    /**
     * 竞赛id(主键)
     */
    @TableId(value = "EXAM_ID",type = IdType.ASSIGN_ID)
    private Long examId;
    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 竞赛开始时间
     */
    private LocalDateTime startTime;

    /**
     * 竞赛结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否发布 0: 未发布 1: 已发布
     */
    private Integer status;
}