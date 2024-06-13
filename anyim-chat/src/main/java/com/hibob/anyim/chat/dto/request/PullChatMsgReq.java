package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("单聊消息拉取接口的请求体参数")
public class PullChatMsgReq extends BaseRequest {

    @NotEmpty
    @Size(max = 64, message = "账号长度不能大于64字符")
    @ApiModelProperty(value = "单聊的对方id")
    private String toAccount;

    @NotNull
    @ApiModelProperty(value = "可选参数，上次更新msgId，没有就传-1")
    private long lastMsgId;

    @NotNull
    @ApiModelProperty(value = "上次更新的时间，UTC时间，单位毫秒，没有就传-1")
    private long lastPullTime;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;
}
