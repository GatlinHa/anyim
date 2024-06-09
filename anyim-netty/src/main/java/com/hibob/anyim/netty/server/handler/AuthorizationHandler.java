package com.hibob.anyim.netty.server.handler;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
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

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class AuthorizationHandler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        log.info("do AuthorizationHandler ");
        RedisTemplate<String, Object> redisTemplate = SpringContextUtil.getBean("redisTemplate");
        String token = msg.headers().get(HttpHeaderNames.AUTHORIZATION);
        String uniqueId = CommonUtil.conUniqueId(msg.headers().get("account"), msg.headers().get("clientId"));
        String key = RedisKey.USER_ACTIVE_TOKEN + uniqueId;
        String value = (String) redisTemplate.opsForValue().get(key);
        String cacheToken = StringUtils.hasLength(value) ? JSON.parseObject(value).getString("token") : "";
        if (!StringUtils.hasLength(token)
                || !StringUtils.hasLength(uniqueId)
                || !StringUtils.hasLength(cacheToken)
                || !token.equals(cacheToken)) {
            log.error("Authorization validate error");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        String channelKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        // 登录过了就不要重复登录了，1800s登录一次
        //TODO 调试中，允许重复的登录
//        if (redisTemplate.hasKey(channelKey)) {
//            log.error("Repeated login");
//            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, ByteBufAllocator.DEFAULT.heapBuffer());
//            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//        }

        log.info("Authorization validate success");
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
