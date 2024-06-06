package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("更新聊天信息的参数")
public class UpdateReq extends BaseRequest {

    @ApiModelProperty(value = "可选参数，上次更新msgId")
    private long lastMsgId;

    @ApiModelProperty(value = "可选参数，上次更新的时间")
    private int lastUpdateTime;
}
