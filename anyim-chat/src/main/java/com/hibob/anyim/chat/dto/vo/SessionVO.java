package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("session返回的字段")
public class SessionVO {

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "会话id")
    private String sessionId;

    @ApiModelProperty(value = "对方id")
    private String remoteId;

    @ApiModelProperty(value = "会话类型")
    private int sessionType;

    @ApiModelProperty(value = "已读消息id")
    private long readMsgId;

    @ApiModelProperty(value = "已读消息时间")
    private Date readTime;

    @ApiModelProperty(value = "是否置顶")
    private boolean top;

    @ApiModelProperty(value = "是否免打扰")
    private boolean dnd;

    @ApiModelProperty(value = "草稿")
    private String draft;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "加入群组的时间（数组）")
    private List<String> joinTime;

    @ApiModelProperty(value = "离开群组的时间（数组）")
    private List<String> leaveTime;

    @ApiModelProperty(value = "分组id")
    private int partitionId;

    @ApiModelProperty(value = "会话是被被关闭")
    private Boolean closed;

    @ApiModelProperty(value = "对方已读消息id")
    private long remoteRead;

    @ApiModelProperty(value = "是否离开群了")
    private Boolean leave;

    @ApiModelProperty(value = "未读消息数量")
    private int unreadCount;

    @ApiModelProperty(value = "会话对象详情")
    private Map<String, Object> ObjectInfo;
}
