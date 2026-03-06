package com.example.common.core.controller;

import com.example.common.core.domain.R;
import org.springframework.stereotype.Component;

public class BaseController {
    public R<Void> toResult(int rows){
        return rows > 0 ? R.ok():R.fail();
    }

    public R<Void> toResult(boolean result){
        return result ? R.ok():R.fail();
    }
}
