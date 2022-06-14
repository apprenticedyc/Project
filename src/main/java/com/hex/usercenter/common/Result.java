package com.hex.usercenter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Hex
 * @since 2022/6/5
 * Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    /**
     * 业务状态码
     */
    private int code;
    /**
     * 通用数据
     */
    private T data;
    /**
     * 业务执行信息
     */
    private String message;

    /**
     * 错误信息的描述
     */
    private String description;


    public Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static Result ok() {
        return new Result<>(0, null, "ok");
    }

    public static Result ok(Object data) {
        return new Result<>(0, data, "ok");
    }

    public static Result ok(List<?> data) {
        return new Result<>(0, data, "ok","");
    }

    public static Result fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

    public static Result fail(ErrorCode errorCode, String description) {
        return new Result<>(errorCode.getCode(), null, errorCode.getMessage(), description);
    }

    public static Result fail(int code,String message, String description) {
        return new Result<>(code, null, message, description);
    }
}
