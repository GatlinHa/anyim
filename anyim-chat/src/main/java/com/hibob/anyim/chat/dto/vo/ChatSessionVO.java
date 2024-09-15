package com.hibob.anyim.chat.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel("查询单聊会话返回的参数中的元素")
public class ChatSessionVO {

    @ApiModelProperty(value = "会话ID")
    private String sessionId;

    @ApiModelProperty(value = "会话类型CHAT(2)")
    private int sessionType = 2;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "对方详情")
    private Map<String, Object> ObjectInfo;
}
