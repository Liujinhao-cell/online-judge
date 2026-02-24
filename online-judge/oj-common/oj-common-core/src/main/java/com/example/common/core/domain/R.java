package com.example.common.core.domain;

import lombok.Data;

@Data
public class R<T> {
    /**
     *响应码
     */
    private int code;
    /**
     *信息
     */
    private String msg;
    /**
     *响应数据
     */
    private T data;
}
