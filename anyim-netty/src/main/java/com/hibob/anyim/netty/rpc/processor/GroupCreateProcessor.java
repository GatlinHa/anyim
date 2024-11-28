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
import java.util.Map;

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
        String creatorId = (String) msgMap.get("creatorId");
        Map<String, String> members = (Map<String, String>) msgMap.get("members");
        contentMap.put("creatorId", creatorId);
        contentMap.put("members",members);
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(contentMap);

        Header header = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0) //TODO 服务器版本
                .setMsgType(MsgType.SYS_GROUP_CREATE)
                .build();
        Body body = Body.newBuilder()
                .setGroupId(groupId)
                .setMsgId(msgId)
                .setSessionId(sessionId)
                .setContent(content)
                .build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();
        saveMsg(msg, msgId); //这里的系统消息要入库
        sendToMembers(msg, new ArrayList<>(members.keySet()), msgId); // 发给群成员
    }

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg) throws Exception {
        // do nothing
    }
}
