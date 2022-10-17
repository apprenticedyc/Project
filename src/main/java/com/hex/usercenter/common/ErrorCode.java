package com.hex.usercenter.common;

/**
 * 全局错误码
 * @author Hex
 * @since 2022/6/5
 * Description
 */
public enum ErrorCode {
    PARAMS_ERROR(4000,"请求参数错误",""),
    PARAMS_NULL_ERROR(4001,"没有该数据",""),
    NO_AUTHORITY_ERROR(40101,"无权限",""),
    NOT_LOGIN(40100,"未登录",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
