package com.luo.share.common.exception;

import com.luo.share.common.api.Result;
import com.luo.share.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 捕获自定义业务异常
    @ExceptionHandler(value = RuntimeException.class)
    public Result<Object> handle(RuntimeException e) {
        log.error("业务异常: ", e);
        // 有些 Redis 连接异常可能封装得很深，如果是底层异常可以直接返回 e.getMessage()
        return Result.failed(500, e.getMessage());
    }

    // 捕获参数校验异常 (如 @Valid 失败)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Object> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.failed(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
}
