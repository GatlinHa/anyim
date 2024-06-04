package com.hibob.anyim.common.interceptor;

import com.hibob.anyim.common.utils.XssUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;

/**
 * XSS攻击防护拦截器
 */
@Slf4j
@Component
public class XssInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //  检查body
        String body = getBody(request);
        if (XssUtil.checkXss(body)) {
            log.info("the body of request contains Illegal parameters, body is {}", body);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }

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
