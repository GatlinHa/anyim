package com.hibob.anyim.agw.filter;

import com.hibob.anyim.common.utils.XssUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class XssFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("XssFilter start......path is {}", exchange.getRequest().getPath());
        ServerHttpRequest request = exchange.getRequest();

        HttpHeaders headers = request.getHeaders();
        for (List<String> values : headers.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    log.error("The xss verification of the request header is invalid, and the verification object is:{}", value);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                }
            }
        }

        MultiValueMap<String, String> paramMap = request.getQueryParams();
        for (List<String> values : paramMap.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    log.error("The request parameter xss is invalid, and the verification object is as follows:{}", value);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                }
            }
        }

        // 这里不对请求体校验，留给后端服务校验。Spring WebFlux当请求的Body被读取后，它就被消费了，不能再往后面传了。
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
