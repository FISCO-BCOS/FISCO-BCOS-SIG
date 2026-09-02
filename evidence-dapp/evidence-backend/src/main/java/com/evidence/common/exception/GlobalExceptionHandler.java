package com.evidence.common.exception;

import com.evidence.common.R;
import com.evidence.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        log.warn("参数校验失败: {}", message);
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束校验失败: {}", e.getMessage());
        return R.fail(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(BlockchainException.class)
    public R<Void> handleBlockchainException(BlockchainException e) {
        log.error("区块链服务异常: {}", e.getMessage(), e);
        return R.fail(ResultCode.CHAIN_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("服务器内部错误: {}", e.getMessage(), e);
        return R.fail(ResultCode.INTERNAL_ERROR);
    }
}
