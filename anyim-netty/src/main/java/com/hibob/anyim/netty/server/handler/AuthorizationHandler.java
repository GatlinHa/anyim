package com.hibob.anyim.netty.server.handler;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class AuthorizationHandler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        log.info("do AuthorizationHandler ");
        RedisTemplate<String, Object> redisTemplate = SpringContextUtil.getBean("redisTemplate");
        // 获取参数
        Map<String, String> params = Arrays.stream(msg.uri().split("\\?")[1].split("&"))
                .map(param -> {
                    int index = param.indexOf('=');
                    String[] arr = new String[2];
                    if (index!= -1) {
                        arr[0] = param.substring(0, index);
                        arr[1] = param.substring(index + 1);
                    }
                    return arr;
                })
                .collect(java.util.stream.Collectors.toMap(param -> param[0], param -> param[1]));
        String traceId = params.get("traceId");
        String timestamp = params.get("timestamp");
        String sign = params.get("sign");
        String token = params.get("token");
        if (!StringUtils.hasLength(traceId)
                || !StringUtils.hasLength(timestamp)
                || !StringUtils.hasLength(sign)
                || !StringUtils.hasLength(token)) {
            log.error("The ws request params are not correct");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        // timestamp过期
        if (Instant.now().getEpochSecond() - Long.parseLong(timestamp) > Const.REQUEST_SIGN_EXPIRE) {
            log.error("Timestamp exceeds the expiration time");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String account = JwtUtil.getAccount(token);
        String clientId = JwtUtil.getInfo(token);
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        // 防重放攻击
        String reqRecordKey = RedisKey.USER_REQ_RECORD + uniqueId + SPLIT_V + traceId;
        if (redisTemplate.hasKey(reqRecordKey)) {
            log.error("Duplicate ws requests");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String accessTokenKey = RedisKey.USER_ACTIVE_TOKEN + uniqueId;
        String value = (String) redisTemplate.opsForValue().get(accessTokenKey);
        // token被老化
        if (!StringUtils.hasLength(value)) {
            log.error("The access token has been deleted");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        String cacheToken = StringUtils.hasLength(value) ? JSON.parseObject(value).getString("token") : "";
        String cacheSecret = StringUtils.hasLength(value) ? JSON.parseObject(value).getString("secret") : "";
        // token不一致
        if (!StringUtils.hasLength(cacheToken) || !token.equals(cacheToken)) {
            log.error("token inconsistency");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        // 签名不一致
        String generatedSign = JwtUtil.generateSign(cacheSecret, traceId + timestamp);
        if (!sign.equals(generatedSign)) {
            log.error("The checksum sign does not match");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        log.info("Authorization validate success");
        redisTemplate.opsForValue().set(reqRecordKey, "", Duration.ofSeconds(Const.REQUEST_SIGN_EXPIRE)); // 缓存请求记录
        ctx.channel().attr(AttributeKey.valueOf(Const.UNIQUE_ID)).set(uniqueId);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 这里会捕获这个异常io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
        // 因为在channelRead0方法中最后调用ctx.fireChannelRead(msg)会触发以下引用-1，而SimpleChannelInboundHandler还会再次自动回收msg
        // 这个异常没有影响，如果非要解决，可以在ctx.fireChannelRead(msg)前面加上ReferenceCountUtil.retain(msg)，增加一次引用计数
        log.error("AuthorizationHandler Caught a exception, it is {}", cause.toString());
    }
}
