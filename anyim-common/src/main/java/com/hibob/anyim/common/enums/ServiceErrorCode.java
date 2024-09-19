package com.hibob.anyim.common.enums;

import com.hibob.anyim.common.constants.Const;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ServiceErrorCode {
    ERROR_ACCOUNT_EXIST(Const.SERVICE_CODE_USER + 1, "账号已存在"), //TODO 这些都没有利用起来，后面整改下

    ERROR_XSS(Const.SERVICE_CODE_USER + 2, "输入内容含有非法字符"),

    ERROR_NO_LOGIN(Const.SERVICE_CODE_USER + 3, "未登录"),

    ERROR_LOGIN(Const.SERVICE_CODE_USER + 4, "账号或密码错误"),

    ERROR_OLD_PASSWORD_ERROR(Const.SERVICE_CODE_USER + 5, "旧密码错误"),

    ERROR_NEW_PASSWORD_EQUAL_OLD(Const.SERVICE_CODE_USER + 6, "新旧密码相等"),

    ERROR_ACCESS_TOKEN_EXPIRED(Const.SERVICE_CODE_USER + 7, "AccessToken已过期"),

    ERROR_ACCESS_TOKEN_DELETED(Const.SERVICE_CODE_USER + 8, "AccessToken以删除"),

    ERROR_REFRESH_TOKEN(Const.SERVICE_CODE_USER + 9, "RefreshToken错误"),

    ERROR_IS_DEREGISTER(Const.SERVICE_CODE_USER + 10, "账号已注销"),

    ERROR_IMAGE_TOO_BIG(Const.SERVICE_CODE_USER + 11, "图像过大"),

    ERROR_FILE_TOO_BIG(Const.SERVICE_CODE_USER + 12, "文件过大"),

    ERROR_IMAGE_FORMAT_ERROR(Const.SERVICE_CODE_USER + 13, "图片格式错误"),

    ERROR_FILE_UPLOAD_ERROR(Const.SERVICE_CODE_USER + 14, "文件上传失败"),

    ERROR_SERVICE_EXCEPTION(Const.SERVICE_CODE_USER + 50, "服务器内部异常"),

    ERROR_CHAT_REFMSGID_EXCEPTION(Const.SERVICE_CODE_CHAT + 1, "ref msgId异常"),

    ERROR_CHAT_UPDATE_SESSION(Const.SERVICE_CODE_CHAT + 1, "更新session异常"),

    ERROR_GROUP_MNG_NOT_ENOUGH_MEMBER(Const.SERVICE_CODE_GROUP_MNG + 1, "成员数不够"),

    ERROR_GROUP_MNG_NOT_IN_GROUP(Const.SERVICE_CODE_GROUP_MNG + 2, "用户不在群组中"),

    ERROR_GROUP_MNG_EMPTY_PARAM(Const.SERVICE_CODE_GROUP_MNG + 3, "参数为空"),

    ERROR_GROUP_MNG_DEL_GROUP_USER_INVALID(Const.SERVICE_CODE_GROUP_MNG + 4, "该用户删除无效"),

    ERROR_GROUP_MNG_NOT_OWNER(Const.SERVICE_CODE_GROUP_MNG + 5, "非群主"),

    ERROR_GROUP_MNG_NEW_OWNER_NOT_IN_GROUP(Const.SERVICE_CODE_GROUP_MNG + 6, "新群主候选人不在该群内"),

    ERROR_GROUP_MNG_OWNER_TRANSFER_EXCEPTION(Const.SERVICE_CODE_GROUP_MNG + 7, "群主转让发生异常"),

    ERROR_DEFAULT(Const.SERVICE_CODE_DEFAULT, "未知错误");


    private int code;
    private String desc;

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
