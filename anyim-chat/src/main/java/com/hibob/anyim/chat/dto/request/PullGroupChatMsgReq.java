package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("群聊消息拉取接口的")
public class PullGroupChatMsgReq extends BaseRequest {

    @NotNull
    @ApiModelProperty(value = "群id")
    private long groupId;

    @NotNull
    @ApiModelProperty(value = "可选参数，上次更新msgId，没有就传-1")
    private long lastMsgId;

    @NotNull
    @ApiModelProperty(value = "上次更新的时间，UTC时间，单位秒，没有就传-1")
    private long lastPullTime;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;
}
