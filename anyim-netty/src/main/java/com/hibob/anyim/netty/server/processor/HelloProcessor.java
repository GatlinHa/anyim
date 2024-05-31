package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.hibob.anyim.common.constants.Const.SPLIT_C;
import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;


@Component
public class HelloProcessor implements MsgProcessor{

    @Autowired
    private NacosConfig nacosConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        writeCache(ctx);
        Header headerOut = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0)
                .setMsgType(MsgType.HELLO)
                .build();
        Msg msgOut = Msg.newBuilder().setHeader(headerOut).build();
        ctx.writeAndFlush(msgOut);
    }

    private void writeCache(ChannelHandlerContext ctx) {
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.UNIQUE_ID)).get();
        String account = uniqueId.split(SPLIT_V)[0];
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        String instance = CommonUtil.getLocalIp() + SPLIT_C + nacosConfig.getPort();
        redisTemplate.opsForValue().set(routeKey, instance, Duration.ofSeconds(Const.CHANNEL_EXPIRE)); //TODO 如果老化了，怎办么？
        redisTemplate.opsForSet().add(onlineKey, uniqueId);
        redisTemplate.expire(onlineKey, Duration.ofSeconds(Const.CACHE_ONLINE_EXPIRE));
        getLocalRoute().put(routeKey, ctx.channel());
    }
}
