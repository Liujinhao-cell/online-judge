package com.example.common.security.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.core.domain.R;
import com.example.common.core.enums.ResultCode;
import com.example.common.security.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 请求⽅式不⽀持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                        HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不⽀持'{}'请求", requestURI, e.getMethod());
        return R.fail(ResultCode.ERROR);
    }

    /**
     * 自定义异常
     * @param e 异常
     * @param request 响应
     * @return {@link R }<{@link ? }>
     */
    @ExceptionHandler(ServiceException.class)
    public R<?> handleServiceException(ServiceException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // 获取异常信息
        Integer code = e.getCode();
        String message = e.getMessage();
        log.error("请求地址'{}',发⽣业务异常: code={}, message={}", requestURI, code, message, e);
        // 如果有code，使用code和message返回
        if (code != null) {
            return R.fail(code, message);
        }
        // 如果有resultCode，使用resultCode返回
        ResultCode resultCode = e.getResultCode();
        if (resultCode != null) {
            return R.fail(resultCode);
        }
        // 默认返回系统错误
        return R.fail(ResultCode.ERROR);
    }
//    @ExceptionHandler(ServiceException.class)
//    public R<?> handleServiceException(ServiceException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        ResultCode resultCode = e.getResultCode();
//        log.error("请求地址'{}',发⽣业务异常:{}", requestURI,resultCode.getMsg(), e);
//        return R.fail(resultCode);
//    }

    /**
     * 参数校验时异常
     * @param e 参数校验时异常
     * @return {@link R }<{@link Void }>
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        log.error(e.getMessage());
        String message = join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return R.fail(ResultCode.FAILED_PARAMS_VALIDATE.getCode(), message);
    }

    // 处理参数类型转换异常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("参数 '%s' 类型错误，期望类型为 %s", e.getName(), e.getRequiredType().getSimpleName());
        return R.fail(ResultCode.HANDLE_METHOD_ARGUMENT_TYPE_MISMATCH.getCode(),msg);  // 返回自定义错误对象
    }

    private <E> String join(Collection<E> collection, Function<E, String> function, CharSequence delimiter) {
        if (CollUtil.isEmpty(collection)) {
            return StrUtil.EMPTY;
        }
        return collection.stream().map(function).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }
    /**
     * 拦截运⾏时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException e, HttpServletRequest
            request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发⽣异常.", requestURI, e);
        return R.fail(ResultCode.ERROR);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发⽣异常.", requestURI, e);
        return R.fail(ResultCode.ERROR);
    }
}