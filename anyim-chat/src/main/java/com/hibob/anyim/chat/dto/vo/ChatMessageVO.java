package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("查询聊天（单聊/群聊）会话返回的参数中的元素")
public class ChatMessageVO {

    @ApiModelProperty(value = "消息总条数")
    private int count;

    @ApiModelProperty(value = "未读消息条数")
    private int unreadCount;

    @ApiModelProperty(value = "本次返回首条消息id")
    private long firstMsgId;

    @ApiModelProperty(value = "本次返回尾条消息id")
    private long lastMsgId;

    @ApiModelProperty(value = "消息列表")
    private List<MsgVO> msgList;
}
