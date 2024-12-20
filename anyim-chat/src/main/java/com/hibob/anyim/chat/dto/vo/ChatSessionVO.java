package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@ApiModel("查询聊天（单聊/群聊）会话返回的参数中的元素")
public class ChatSessionVO {

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "会话类型CHAT(2)")
    private int sessionType;

    @ApiModelProperty(value = "会话的对方Id，单聊为对方账号，群聊为groupId")
    private String remoteId;

    @ApiModelProperty(value = "已读消息Id")
    private long readMsgId;

    @ApiModelProperty(value = "已读时间")
    private Date readTime;

    @ApiModelProperty(value = "对方已读到哪条消息了")
    private long remoteRead;

    @ApiModelProperty(value = "最后一条消息的MsgId")
    private long lastMsgId;

    @ApiModelProperty(value = "最后一条消息内容")
    private String lastMsgContent;

    @ApiModelProperty(value = "最后一条消息的发送者id")
    private String lastMsgAccount;

    @ApiModelProperty(value = "最后一条消息的类型")
    private int lastMsgType;

    @ApiModelProperty(value = "最后一条消息的时间")
    private Date lastMsgTime;

    @ApiModelProperty(value = "未读消息数量")
    private int unreadCount;

    @ApiModelProperty(value = "会话是否置顶")
    private boolean top;

    @ApiModelProperty(value = "会话是免打扰")
    private boolean dnd;

    @ApiModelProperty(value = "草稿")
    private String draft;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "分组id")
    private int partitionId;

    @ApiModelProperty(value = "离群状态")
    private boolean leaveFlag;

    @ApiModelProperty(value = "离群时消息Id")
    private long leaveMsgId;

    @ApiModelProperty(value = "对方详情")
    private Map<String, Object> ObjectInfo;
}
