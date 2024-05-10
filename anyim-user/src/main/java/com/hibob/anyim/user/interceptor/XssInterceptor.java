package com.hibob.anyim.user.interceptor;

import com.hibob.anyim.common.utils.XssUtil;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import com.hibob.anyim.user.exception.ServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.Map;

@Slf4j
@Component
public class XssInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 检查参数
        Map<String, String[]> paramMap = request.getParameterMap();
        for (String[] values : paramMap.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    throw new ServiceException(ServiceErrorCode.ERROR_XSS);
                }
            }
        }
        //  检查body
//        String body = getBody(request);
//        if (XssUtils.checkXss(body)) {
//            throw new ServiceException(ServiceErrorCode.ERROR_XSS);
//        }
        return true;
    }

    @SneakyThrows
    private String getBody(HttpServletRequest request) {
        BufferedReader reader = request.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
