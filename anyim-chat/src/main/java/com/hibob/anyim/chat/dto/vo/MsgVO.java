package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("单聊/群聊记录返回的字段")
public class MsgVO {

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "发送方Id")
    private String fromId;

    @ApiModelProperty(value = "消息Id")
    private long msgId;

    @ApiModelProperty(value = "消息类型")
    private int msgType;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息时间")
    private Date msgTime;
}
