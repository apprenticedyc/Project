package com.hex.usercenter.exception;

import com.hex.usercenter.common.ErrorCode;
import com.hex.usercenter.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理拦截器
 *
 * @author Hex
 * @since 2022/6/5
 * Description
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result businessExceptionHandler(BusinessException e) {
        log.error("businessException: "+e.getDescription(),e);
        return Result.fail(e.getCode(),e.getMessage(),e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result runtimeExceptionHandler(BusinessException e) {
        log.error("runtimeException", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
