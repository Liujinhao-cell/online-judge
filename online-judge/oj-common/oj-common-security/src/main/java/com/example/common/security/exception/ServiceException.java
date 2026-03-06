package com.example.common.security.exception;

import com.example.common.core.enums.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException{
    private ResultCode resultCode;

    public ServiceException(ResultCode resultCode){
        this.resultCode = resultCode;
    }
}
