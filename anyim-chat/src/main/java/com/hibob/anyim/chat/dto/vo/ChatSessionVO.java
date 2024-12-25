package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("单聊/群聊返回的会话列表详情")
public class ChatSessionVO {

    @ApiModelProperty(value = "会话详情")
    private SessionVO session;

    @ApiModelProperty(value = "消息列表")
    private List<MsgVO> msgList;
}
