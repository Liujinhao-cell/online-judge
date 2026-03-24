package com.example.api;

import lombok.Data;

@Data
public class UserExeResult {

    private String input;

    /**
     * 期望输出
     */
    private String output;

    /**
     *实际输出
     */
    private String exeOut;
}
