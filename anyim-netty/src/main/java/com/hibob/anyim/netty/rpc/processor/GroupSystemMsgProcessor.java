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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GroupSystemMsgProcessor extends MsgProcessor implements SystemMsgProcessor {

    private final RefMsgIdConfig refMsgIdConfig;
    private final RpcClient rpcClient;

    @Override
    public void processSystemMsg(Map<String, Object> msgMap) throws Exception {
        MsgType msgType = MsgType.forNumber((Integer) msgMap.get("msgType"));
        String groupId = (String) msgMap.get("groupId");
        Long msgId = refMsgIdConfig.generateMsgId(groupId);
        Map<String, Object> contentMap = (Map<String, Object>) msgMap.get("content");
        ObjectMapper objectMapper = new ObjectMapper();
        String content = objectMapper.writeValueAsString(contentMap);

        Header header = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0) //TODO 服务器版本
                .setMsgType(msgType)
                .build();
        Body body = Body.newBuilder()
                .setGroupId(groupId)
                .setMsgId(msgId)
                .setSessionId(groupId)
                .setContent(content)
                .build();
        Msg msg = Msg.newBuilder().setHeader(header).setBody(body).build();
        saveMsg(msg, msgId); //这里的系统消息要入库

        List<String> members;
        if (msgType == MsgType.SYS_GROUP_UPDATE_MEMBER_MUTED) {
            // 设置某个成员禁言，消息只发给被禁言或取消禁言的成员
            String account = ((Map<String, String>)contentMap.get("member")).get("account");
            members = new ArrayList<>();
            members.add(account);
        } else if (msgType == MsgType.SYS_GROUP_DROP) {
            // 群组已经解散，查不到成员信息了
            members = (List<String>) msgMap.get("toAccounts");
        }
        else {
            members = rpcClient.getGroupMngRpcService().queryGroupMembers(groupId);
        }

        sendToMembers(msg, members, msgId); // 发给群成员
    }

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg) throws Exception {
        // do nothing
    }
}
