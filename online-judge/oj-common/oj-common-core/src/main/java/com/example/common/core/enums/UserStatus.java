package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatus {
    NORMAL(1),
    BLOCK(0);

    private Integer value;

}
