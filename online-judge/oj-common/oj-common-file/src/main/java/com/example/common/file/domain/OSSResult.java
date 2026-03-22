package com.example.common.file.domain;

import lombok.Data;

@Data
public class OSSResult {
    /**
     *
     */
    private String name;
    /**
     * 可选状态:true 成功 false 失败
     */
    private boolean success;
    private String url;
    private String key;
}
