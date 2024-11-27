package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.netty.config.RefMsgIdConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChatReadProcessor extends MsgProcessor{
    private final RefMsgIdConfig refMsgIdConfig;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String sessionId = msg.getBody().getSessionId();
        Long msgId = refMsgIdConfig.generateMsgId(sessionId); // 生成msgId
        syncOtherClients(msg, msgId); // 扩散给自己的其他客户端，群聊消息已读不发给其他成员
        updateReadMsgId(fromId, sessionId, msg.getBody().getContent()); // 已读消息不入库，只记录最新的已读消息
    }
}
