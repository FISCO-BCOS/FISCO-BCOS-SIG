package com.points.exception;

import com.points.enums.ErrorCodeEnum;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException() {
        super(ErrorCodeEnum.INSUFFICIENT_BALANCE);
    }

    public InsufficientBalanceException(String message) {
        super(ErrorCodeEnum.INSUFFICIENT_BALANCE, message);
    }
}