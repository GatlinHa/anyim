package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.netty.config.RefMsgIdConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupChatProcessor extends MsgProcessor{
    private final RpcClient rpcClient;
    private final RefMsgIdConfig refMsgIdConfig;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String groupId = msg.getBody().getGroupId();
        List<String> members = rpcClient.getGroupMngRpcService().queryGroupMembers(groupId);
        String fromId = msg.getBody().getFromId();
        if (!members.contains(fromId)) {
            log.error("fromId is invalid: {}", fromId);
            return;
        }
        String sessionId = msg.getBody().getSessionId();
        Long msgId = refMsgIdConfig.generateMsgId(sessionId); // 生成msgId
        saveMsg(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
        sendDeliveredMsg(ctx, msg, msgId); //回复已送达
        syncOtherClients(msg, msgId); // 扩散给自己的其他客户端
        sendToMembers(msg, members, msgId); // 发给群成员
    }
}
