package com.trace.model;

import com.trace.model.vo.ResultVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "姝ｅ父", data);
    }

    public static <T> Result<T> error(ResultVO resultVO) {
        return new Result<>(resultVO.getCode(), resultVO.getMsg(), null);
    }

    /** 通用错误消息（用于安全加固等场景，避免暴露内部细节） */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}