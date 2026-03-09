package com.example.common.core.domain;

import lombok.Data;

@Data
public class PageQueryDTO {
    /**
     *每页的数据 默认10
     */
    private Integer pageSize = 10 ;

    /**
     *第几页 默认1
     */
    private Integer pageNum = 1;
}
