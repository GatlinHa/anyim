package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("更新聊天信息的参数")
public class PullChatMsgReq extends BaseRequest {

    @NotEmpty
    @Size(max = 64, message = "账号长度不能大于64字符")
    @ApiModelProperty(value = "单聊的对方id")
    private String toAccount;

    @ApiModelProperty(value = "可选参数，上次更新msgId")
    private long lastMsgId;

    @ApiModelProperty(value = "可选参数，上次更新的时间，UTC时间，单位秒")
    private long lastPullTime;
}
