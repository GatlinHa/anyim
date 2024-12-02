package com.hibob.anyim.netty.rpc.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.protobuf.Body;
import com.hibob.anyim.common.protobuf.Header;
import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.netty.config.RefMsgIdConfig;
import com.hibob.anyim.netty.server.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GroupChangeRoleProcessor extends MsgProcessor implements SystemMsgProcessor {

    private final RefMsgIdConfig refMsgIdConfig;
    private final RpcClient rpcClient;

    @Override
    public void processSystemMsg(Map<String, Object> msgMap) throws Exception {
        String groupId = (String) msgMap.get("groupId");
        Long msgId = refMsgIdConfig.generateMsgId(groupId);

        Map<String, Object> contentMap = new HashMap<>();
        Map<String, String> operator = (Map<String, String>) msgMap.get("operator");
        Map<String, Object> member = (Map<String, Object>) msgMap.get("member");
        contentMap.put("operator", operator);
        contentMap.put("member",member);
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(contentMap);

        Header header = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0) //TODO 服务器版本
                .setMsgType(MsgType.forNumber((Integer) msgMap.get("msgType")))
                .build();
        Body body = Body.newBuilder()
                .setGroupId(groupId)
                .setMsgId(msgId)
                .setSessionId(groupId)
                .setContent(content)
                .build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();
        saveMsg(msg, msgId); //这里的系统消息要入库
        sendToMembers(msg, rpcClient.getGroupMngRpcService().queryGroupMembers(groupId), msgId); // 发给群成员
    }

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg) throws Exception {
        // do nothing
    }
}
