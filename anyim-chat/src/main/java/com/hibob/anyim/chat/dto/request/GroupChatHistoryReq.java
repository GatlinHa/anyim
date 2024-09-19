package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("群聊历史消息查询接口的请求体参数")
public class GroupChatHistoryReq extends BaseRequest {

    @NotNull
    @ApiModelProperty(value = "群id")
    private long groupId;

    @NotNull
    @ApiModelProperty(value = "查询开始时间，UTC毫秒")
    private long startTime;

    @NotNull
    @ApiModelProperty(value = "查询结束时间，UTC毫秒")
    private long endTime;

    @NotNull
    @ApiModelProperty(value = "可选参数，已读msgId，没有就传-1")
    private long readMsgId;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;

}
