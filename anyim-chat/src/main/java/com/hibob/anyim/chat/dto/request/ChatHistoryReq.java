package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("单聊历史消息查询接口的请求体参数")
public class ChatHistoryReq extends BaseRequest {

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,32}$", message = "账号长度必须是6-32位的字母、数字或下划线")
    @ApiModelProperty(value = "单聊的对方id")
    private String toAccount;

    @NotNull
    @ApiModelProperty(value = "查询开始时间，UTC毫秒")
    private long startTime;

    @NotNull
    @ApiModelProperty(value = "查询结束时间，UTC毫秒")
    private long endTime;

    @NotNull
    @ApiModelProperty(value = "可选参数，上次更新msgId，没有就传-1")
    private long lastMsgId;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;

}
