package com.trace.exception;

import com.trace.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business: code={}, msg={}", e.getCode(), e.getMsg());
        return new Result<>(e.getCode(), e.getMsg(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数校验失败" : fieldError.getDefaultMessage();
        log.warn("Validation failed: {}", message);
        return new Result<>(400, message, null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e) {
        log.error("系统异常: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        // 生产环境绝不向客户端暴露堆栈信息，仅返回通用错误码
        return new Result<>(500, "服务器内部错误，请稍后重试", null);
    }
}
