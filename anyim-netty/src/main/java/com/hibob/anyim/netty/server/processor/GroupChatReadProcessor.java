package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.protobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupChatReadProcessor extends MsgProcessor{


    @Autowired
    private RpcClient rpcClient;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String groupId = msg.getBody().getGroupId();
        String sessionId = groupId;
        List<String> members = rpcClient.getGroupMngRpcService().queryGroupMembers(groupId);
        Long msgId = computeMsgId(ctx, sessionId); // 生成msgId
        syncOtherClients(msg, sessionId, msgId); // 扩散给自己的其他客户端
        sendToMembers(msg, members, msgId); // 发给群成员
        updateReadMsgId(fromId, sessionId, msg.getBody().getContent()); // 已读消息不入库，只记录最新的已读消息
    }
}
