package com.points.exception;

import com.points.enums.ErrorCodeEnum;

public class ChainOperationException extends BusinessException {
    public ChainOperationException() {
        super(ErrorCodeEnum.CHAIN_ERROR);
    }

    public ChainOperationException(String message) {
        super(ErrorCodeEnum.CHAIN_ERROR, message);
    }
}