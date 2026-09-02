package com.trace.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultVO {
    private int code;
    private String msg;
    private String data;

    public static final ResultVO SUCCESS = new ResultVO(200, "正常", "正常");
    public static final ResultVO PARAM_EMPTY = new ResultVO(404001, "请求参数缺失", "请求参数缺失");
    public static final ResultVO QUERY_EMPTY = new ResultVO(404002, "查询内容不存在", "查询内容不存在");
    public static final ResultVO QUERY_EXISTS = new ResultVO(500001, "信息已存在", "信息已存在");
    public static final ResultVO CONTRACT_ERROR = new ResultVO(500002, "智能合约请求存在问题", "");
    public static final ResultVO TOKEN_EMPTY = new ResultVO(500003, "Token已失效", "Token已失效");
    public static final ResultVO ADDRESS_INVALID = new ResultVO(500004, "操作地址不合法", "操作地址不合法");
    public static final ResultVO USER_NOT_EXIST = new ResultVO(500005, "用户不存在", "用户不存在");
    public static final ResultVO PASSWORD_ERROR = new ResultVO(500006, "密码错误", "密码错误");
    public static final ResultVO BALANCE_NOT_ENOUGH = new ResultVO(500007, "余额不足", "余额不足");
    public static final ResultVO FORBIDDEN = new ResultVO(403001, "无权限执行该操作", "无权限执行该操作");
}
