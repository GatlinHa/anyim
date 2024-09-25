package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import static com.hibob.anyim.common.utils.CommonUtil.combineId;

/**
 * Chat的消息Body：fromId, fromClient, toId, content, tempMsgId
 */
@Component
public class ChatProcessor extends MsgProcessor{

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String toId = msg.getBody().getToId(); //端侧发过来的消息，也不知道要发给哪个client，所以没填toClient
        String sessionId = combineId(fromId, toId);
        Long msgId = computeMsgId(ctx, sessionId); // 生成msgId
        saveMsg(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
        sendDeliveredMsg(ctx, msg, sessionId, msgId); //回复已送达
        syncOtherClients(msg, sessionId, msgId); // 扩散给自己的其他客户端
        if (fromId.equals(toId)) { //自发自收的消息到此为止，不发送（不给toId发送）
            return;
        }
        sendToClients(msg, toId, msgId); // 扩散给接收端的（多个）客户端
    }

}
