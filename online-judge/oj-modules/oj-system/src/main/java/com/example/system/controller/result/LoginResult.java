package com.example.system.controller.result;

import lombok.Data;

@Data
public class LoginResult {
    /**
     *响应码
     */
    private int code;
    /**
     *信息
     */
    private String msg;
}
