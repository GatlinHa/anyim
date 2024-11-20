package com.hibob.anyim.netty.server.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.common.protobuf.MsgType;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatusReqProcessor extends MsgProcessor{

    @Autowired
    private RpcClient rpcClient;
    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String content = msg.getBody().getContent();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> accounts = Arrays.stream(objectMapper.readValue(content, String[].class)).collect(Collectors.toList());
        Map<String, Integer> statusMap = rpcClient.getUserRpcService().queryUserStatus(accounts);

        Msg msgOut = Msg.newBuilder(msg)
                .setHeader(msg.getHeader().toBuilder()
                        .setMsgType(MsgType.STATUS_RES)
                        .build())
                .setBody(msg.getBody().toBuilder()
                        .setContent(objectMapper.writeValueAsString(statusMap))
                        .build())
                .build();
        ctx.writeAndFlush(msgOut);
    }

}
