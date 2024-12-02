package com.hibob.anyim.netty.rpc.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.protobuf.Body;
import com.hibob.anyim.common.protobuf.Header;
import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.netty.config.RefMsgIdConfig;
import com.hibob.anyim.netty.server.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupCreateProcessor extends MsgProcessor implements SystemMsgProcessor {

    private final RefMsgIdConfig refMsgIdConfig;
    @Override
    public void processSystemMsg(Map<String, Object> msgMap) throws Exception {
        String groupId = (String) msgMap.get("groupId");
        String sessionId = groupId;
        Long msgId = refMsgIdConfig.generateMsgId(sessionId);

        Map<String, Object> contentMap = new HashMap<>();
        Map<String, String> operator = (Map<String, String>) msgMap.get("operator");
        List<Map<String, Object>> members = (List<Map<String, Object>>) msgMap.get("members");
        contentMap.put("operator", operator);
        contentMap.put("members",members);
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
                .setSessionId(sessionId)
                .setContent(content)
                .build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();
        saveMsg(msg, msgId); //这里的系统消息要入库
        List<String> memberAccounts = members.stream().map(item -> (String)item.get("account")).collect(Collectors.toList());
        sendToMembers(msg, memberAccounts, msgId); // 发给群成员
    }

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg) throws Exception {
        // do nothing
    }
}
