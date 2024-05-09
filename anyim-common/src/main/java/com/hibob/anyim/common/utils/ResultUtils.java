package com.hibob.anyim.common.utils;

import com.hibob.anyim.common.model.IMHttpResponse;

public final class ResultUtils {
    private ResultUtils() {
    }

    public static IMHttpResponse success() {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(0);
        response.setDesc("success");
        return response;
    }

    public static IMHttpResponse success(Object data) {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(0);
        response.setDesc("success");
        response.setData(data);
        return response;
    }

    public static IMHttpResponse error(int code, String desc) {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(code);
        response.setDesc(desc);
        return response;
    }
}
