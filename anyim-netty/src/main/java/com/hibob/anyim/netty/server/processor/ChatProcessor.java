package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.netty.config.RefMsgIdConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatProcessor extends MsgProcessor{
    private final RefMsgIdConfig refMsgIdConfig;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String toId = msg.getBody().getToId(); //端侧发过来的消息，也不知道要发给哪个client，所以没填toClient
        String sessionId = msg.getBody().getSessionId();
        Long msgId = refMsgIdConfig.generateMsgId(sessionId); // 生成msgId
        saveMsg(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
        sendDeliveredMsg(ctx, msg, msgId); //回复已送达
        syncOtherClients(msg, msgId); // 扩散给自己的其他客户端
        if (fromId.equals(toId)) { //自发自收的消息到此为止，不发送（不给toId发送）
            return;
        }
        sendToClients(msg, toId, msgId); // 扩散给接收端的（多个）客户端
    }

}
