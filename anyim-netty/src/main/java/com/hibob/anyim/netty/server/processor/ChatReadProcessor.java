package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import static com.hibob.anyim.common.utils.CommonUtil.combineId;

/**
 * ChatRead的消息Body：fromId, fromClient, toId, content（content存放的是已读的原MsgId）
 */
@Component
public class ChatReadProcessor extends MsgProcessor{

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String toId = msg.getBody().getToId();
        String sessionId = combineId(fromId, toId);
        Long msgId = computeMsgId(ctx, sessionId); // 生成msgId
        syncOtherClients(msg, sessionId, msgId); // 扩散给自己的其他客户端
        sendToClients(msg, toId, msgId); // 扩散给接收端的（多个）客户端
        updateReadMsgId(fromId, sessionId, msg.getBody().getContent()); // 已读消息不入库，只记录最新的已读消息
        updateRemoteRead(toId, sessionId, msg.getBody().getContent());
    }
}
