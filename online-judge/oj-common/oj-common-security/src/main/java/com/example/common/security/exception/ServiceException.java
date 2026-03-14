package com.example.common.security.exception;

import com.example.common.core.enums.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException{
    private ResultCode resultCode;
    private Integer code;
    private String message;

    public ServiceException(ResultCode resultCode){
        super(resultCode.getMsg()); // 调用父类构造函数
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    public ServiceException(Integer code, String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    public ServiceException(ResultCode resultCode, String message){
        super(message);
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.message = message;
    }
}
