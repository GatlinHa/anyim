package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
@ApiModel("单聊消息拉取接口的请求体参数")
public class PullChatMsgReq extends BaseRequest {

    @NotEmpty
    @ApiModelProperty(value = "会话Id")
    private String sessionId;

    @NotNull
    @ApiModelProperty(value = "可选参数，已读msgId，没有就传-1")
    private long readMsgId;

    @NotNull
    @ApiModelProperty(value = "已读的时间，UTC时间，单位毫秒")
    private Date readTime;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;
}
