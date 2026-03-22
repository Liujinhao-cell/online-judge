package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    /**
     * 定义状态码
     */
    //操作成功
    SUCCESS(1000, "操作成功"),
    //服务器内部错误，友好提⽰
    ERROR(2000, "服务繁忙请稍后重试"),
    HANDLE_METHOD_ARGUMENT_TYPE_MISMATCH(2001,"参数类型转换异常"),
    //操作失败，但是服务器不存在异常
    FAILED(3000, "操作失败"),
    FAILED_UNAUTHORIZED(3001, "未授权"),
    FAILED_PARAMS_VALIDATE(3002, "参数校验失败"),
    FAILED_NOT_EXISTS(3003, "资源不存在"),
    FAILED_ALREADY_EXISTS(3004, "资源已存在"),

    AILED_USER_EXISTS(3101, "⽤户已存在"),
    FAILED_USER_NOT_EXISTS(3102, "⽤户不存在"),
    FAILED_LOGIN(3103, "⽤户名或密码错误"),
    FAILED_USER_BANNED(3104, "您已被列⼊⿊名单, 请联系管理员."),
    FAILED_USER_PHONE(3105,"您输入的手机号有误"),
    FAILED_USER_EMAIL(3106,"您输入的邮箱有误"),
    SEND_TIME_LIMIT(3107,"当天请求次数已达到上限"),
    FAILED_TOO_FREQUENT(3108,"验证码发送过于频繁"),
    FAILED_ERROR_CODE(3109,"验证码错误"),
    FAILED_INVALID_CODE(3110,"验证码无效"),

    EXAM_START_TIME_BEFORE_CURRENT_TIME(3201,"竞赛时间不能早于当前时间"),
    EXAM_START_TIME_AFTER_END_TIME(3202,"竞赛时间不能晚于开始时间"),
    EXAM_NOT_EXISTS(3203, "竞赛不存在"),
    EXAM_QUESTION_NOT_EXISTS(3204,"为竞赛新增的题目不存在"),
    EXAM_STARTED(3205,"竞赛已经开始，不能操作"),
    EXAM_NOT_HAS_QUESTION(3206,"竞赛中不包含题目"),
    EXAM_IS_FINISH(3207,"竞赛已经结束，不能进行操作"),
    QUESTION_ALREADY_IN_EXAM(3208,"部分题目已在竞赛中，请勿重复添加"),
    EXAM_ALREADY_STARTED_QUESTION_CAN_NOT_ADD(3209,"竞赛已开始，不能添加题目"),

    USER_EXAM_HAS_ENTER(3301,"用户已经报过名，无需报名"),

    FAILED_FILE_UPLOAD(3401,"文件上传失败"),
    FAILED_FILE_UPLOAD_TIME_LIMIT(3402,"当天文件上传次数达到上限");
    /**
     * 状态码
     */
    private int code;

    /**
     * 状态描述
     */
    private String msg;
}