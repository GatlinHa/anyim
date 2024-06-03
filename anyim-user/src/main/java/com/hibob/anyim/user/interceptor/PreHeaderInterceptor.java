package com.hibob.anyim.user.interceptor;

import com.hibob.anyim.common.session.UserSession;
import com.hibob.anyim.common.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@AllArgsConstructor
public class PreHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean isRefreshToken = request.getRequestURI().equals("/user/refreshToken");
        String token = isRefreshToken ? request.getHeader("refreshToken") : request.getHeader("accessToken");
        String account = JwtUtil.getAccount(token);
        String clientId = JwtUtil.getInfo(token);
        // 存放session
        UserSession userSession = new UserSession();
        userSession.setAccount(account);
        userSession.setClientId(clientId);
        request.setAttribute("session", userSession);
        return true;
    }

}
