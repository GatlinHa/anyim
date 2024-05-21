package com.hibob.anyim.netty.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class LoginHandler extends SimpleChannelInboundHandler<HttpRequest> {
    private String path;
    public LoginHandler(String path) {
        super();
        this.path = path;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        log.info("do LoginHandler ");
        String uri = msg.uri();
        String authCode = msg.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (!StringUtils.hasLength(uri)
                || !StringUtils.hasLength(authCode)
                || !uri.equals(this.path)
                || !authCode.equals("123")) {
            log.info("Login validate error");
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED, ByteBufAllocator.DEFAULT.heapBuffer());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        log.info("Login validate success");
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 这里会捕获这个异常io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
        // 因为在channelRead0方法中最后调用ctx.fireChannelRead(msg)会触发以下引用-1，而SimpleChannelInboundHandler还会再次自动回收msg
        // 这个异常没有影响，如果非要解决，可以在ctx.fireChannelRead(msg)前面加上ReferenceCountUtil.retain(msg)，增加一次引用计数
        log.error("LoginHandler Caught a exception, it is {}", cause.toString());
    }
}
