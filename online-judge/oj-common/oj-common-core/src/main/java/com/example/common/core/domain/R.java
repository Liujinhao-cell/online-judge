package com.example.common.core.domain;

import com.example.common.core.enums.ResultCode;
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

    /**
     * @return {@link R }<{@link T }>
     */
    public static <T> R<T> ok(){
        return assembleResult(null,ResultCode.SUCCESS);
    }

    /**
     * @param data
     * @return {@link R }<{@link T }>
     */
    public static <T> R<T> ok(T data){
        return assembleResult(data,ResultCode.SUCCESS);
    }

    /**
     * @return {@link R }<{@link T }>
     */
    public static <T> R<T> fail(){
        return assembleResult(null,ResultCode.FAILED);
    }

    public static <T> R<T> fail(int code,String msg){
        return assembleResult(code,msg,null);
    }
    /**
     * @param resultCode
     * @return {@link R }<{@link T }>
     */
    public static <T> R<T> fail(ResultCode resultCode){
        return assembleResult(null,resultCode);
    }

    /**
     * @param data
     * @param resultCode
     * @return {@link R }<{@link T }>
     */
    public static <T> R<T> assembleResult(T data, ResultCode resultCode){
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setData(data);
        r.setMsg(resultCode.getMsg());
        return r;
    }
    public static <T> R<T> assembleResult(int code,String msg,T data){
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }
}