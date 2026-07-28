package com.trace.exception;

import com.trace.model.vo.ResultVO;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String msg;

    public BusinessException(ResultVO resultVO) {
        super(resultVO.getMsg());
        this.code = resultVO.getCode();
        this.msg = resultVO.getMsg();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}