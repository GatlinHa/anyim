package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("单聊消息拉取接口的")
public class PullChatMsgReq extends BaseRequest {

    @NotEmpty
    @Size(max = 64, message = "账号长度不能大于64字符")
    @ApiModelProperty(value = "单聊的对方id")
    private String toAccount;

    @NotEmpty
    @ApiModelProperty(value = "可选参数，上次更新msgId，没有就传-1")
    private long lastMsgId;

    @NotEmpty
    @ApiModelProperty(value = "可选参数，上次更新的时间，UTC时间，单位秒，没有就传-1")
    private long lastPullTime;

    @ApiModelProperty(value = "可选参数，每次拉取的消息数量")
    private int pullCount;
}
