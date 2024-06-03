package com.hibob.anyim.agw.filter;

import com.hibob.anyim.common.utils.XssUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@AllArgsConstructor
public class XssFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        HttpHeaders headers = request.getHeaders();
        for (List<String> values : headers.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    log.info("请求头xss校验不合法，校验对象是：{}", value);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                }
            }
        }

        MultiValueMap<String, String> paramMap = request.getQueryParams();
        for (List<String> values : paramMap.values()) {
            for (String value : values) {
                if (XssUtil.checkXss(value)) {
                    log.info("请求参数xss校验不合法，校验对象是：{}", value);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return exchange.getResponse().setComplete();
                }
            }
        }

        String bodyStr = getBody(request);
        if (StringUtils.hasLength(bodyStr) && XssUtil.checkXss(bodyStr)) {
            log.info("请求体xss校验不合法，校验对象是：{}", bodyStr);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * @return 请求体
     */
    private String getBody(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        return bodyRef.get();
    }


    private String decodeBody(List<DataBuffer> body) {
        StringBuilder sb = new StringBuilder();
        body.forEach(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        });
        return sb.toString();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
