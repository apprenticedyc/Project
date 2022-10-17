package com.hex.usercenter.exception;

import com.hex.usercenter.common.ErrorCode;

/**
 * 自定义业务异常
 *
 * @author Hex
 * @since 2022/6/5
 * Description
 */
public class BusinessException extends RuntimeException {
    private int code;
    private String description;

    public BusinessException(String message, ErrorCode errorCode, String description) {
        super(message);
        this.code = errorCode.getCode();
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
