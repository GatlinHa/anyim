package com.hibob.anyim.common.utils;

import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResultUtil {
    private ResultUtil() {
    }

    public static ResponseEntity<IMHttpResponse> success() {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(0);
        response.setDesc("success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<IMHttpResponse> success(Object data) {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(0);
        response.setDesc("success");
        response.setData(data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<IMHttpResponse> error(HttpStatus status) {
        return new ResponseEntity<>(status);
    }

    public static ResponseEntity<IMHttpResponse> error(ServiceErrorCode errorCode) {
        IMHttpResponse response = new IMHttpResponse();
        response.setCode(errorCode.code());
        response.setDesc(errorCode.desc());
        return new ResponseEntity<>(response, HttpStatus.OK); //自定义码实在HttpStatus.OK基础上返回的下一层code
    }
}
