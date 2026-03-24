package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProgramType {
    JAVA(0,"java语言"),
    CPP(1,"C++语言"),
    PYTHON(2,"python语言");

    private Integer value;

    private String desc;
}
