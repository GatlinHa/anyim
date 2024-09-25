package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hibob.anyim.common.utils.CommonUtil.combineId;

/**
 * Read的消息Body：fromId, fromClient, toId/groupId, content（content存放的是已读的原MsgId）
 */
@Component
public class ReadProcessor extends MsgProcessor{


    @Autowired
    private RpcClient rpcClient;

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        if (msg.getHeader().getMsgType() == MsgType.CHAT) {
            String toId = msg.getBody().getToId();
            String sessionId = combineId(fromId, toId);
            Long msgId = computeMsgId(ctx, sessionId); // 生成msgId
            saveMsg(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
            sendDeliveredMsg(ctx, msg, sessionId, msgId); //回复已送达
            syncOtherClients(msg, sessionId, msgId); // 扩散给自己的其他客户端
            sendToClients(msg, toId, msgId); // 扩散给接收端的（多个）客户端
            updateReadMsgId(fromId, sessionId, msg.getBody().getContent()); // 更新session表中的已读信息
        }
        else if (msg.getHeader().getMsgType() == MsgType.GROUP_CHAT) {
            long groupId = msg.getBody().getGroupId();
            String sessionId = String.valueOf(groupId);
            List<String> members = rpcClient.getGroupMngRpcService().queryGroupMembers(groupId);
            Long msgId = computeMsgId(ctx, sessionId); // 生成msgId
            saveMsg(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
            sendDeliveredMsg(ctx, msg, sessionId, msgId); //回复已送达
            syncOtherClients(msg, sessionId, msgId); // 扩散给自己的其他客户端
            sendToMembers(msg, members, msgId); // 发给群成员
            updateReadMsgId(fromId, sessionId, msg.getBody().getContent()); // 更新session表中的已读信息
        }
    }

    private void updateReadMsgId(String fromId, String sessionId, String readMsgId) {
        Map<String, Object> map = new HashMap<>();
        map.put("account", fromId);
        map.put("sessionId", sessionId);
        map.put("readMsgId", readMsgId);
        map.put("readTime", new Date());
        rpcClient.getChatRpcService().updateReadMsgId(map);
    }
}
