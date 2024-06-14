package com.hibob.anyim.common.enums;

import com.hibob.anyim.common.constants.Const;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ServiceErrorCode {
    ERROR_ACCOUNT_EXIST(Const.SERVICE_CODE_USER + 1, "账号已存在"), //TODO 这些都没有利用起来，后面整改下

    ERROR_XSS(Const.SERVICE_CODE_USER + 2, "输入内容含有非法字符"),

    ERROR_NO_LOGIN(Const.SERVICE_CODE_USER + 3, "未登录"),

    ERROR_MULTI_LOGIN(Const.SERVICE_CODE_USER + 4, "重复登录"),

    ERROR_NO_REGISTER(Const.SERVICE_CODE_USER + 5, "账号未注册"),

    ERROR_PASSWORD(Const.SERVICE_CODE_USER + 6, "密码错误"),

    ERROR_ACCESS_TOKEN_EXPIRED(Const.SERVICE_CODE_USER + 7, "AccessToken已过期"),

    ERROR_ACCESS_TOKEN_DELETED(Const.SERVICE_CODE_USER + 8, "AccessToken以删除"),

    ERROR_REFRESH_TOKEN(Const.SERVICE_CODE_USER + 9, "RefreshToken错误"),

    ERROR_IS_DEREGISTER(Const.SERVICE_CODE_USER + 10, "账号已注销"),

    ERROR_SERVICE_EXCEPTION(Const.SERVICE_CODE_USER + 50, "服务器内部异常"),

    ERROR_CHAT_REFMSGID_EXCEPTION(Const.SERVICE_CODE_CHAT + 1, "ref msgId异常"),

    ERROR_GROUP_MNG_CREATE_GROUP_NOT_ENOUGH(Const.SERVICE_CODE_GROUP_MNG + 1, "成员数必须大于2"),

    ERROR_GROUP_MNG_QUERY_GROUP_NOT_IN_GROUP(Const.SERVICE_CODE_GROUP_MNG + 2, "成员不在群组中"),;


    private int code;
    private String desc;

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
