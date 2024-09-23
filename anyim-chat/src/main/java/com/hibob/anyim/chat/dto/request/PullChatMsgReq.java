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
    @Max(value = 1, message = "页大小不能大于100")
    @Min(value = 0, message = "页大小不能小于10")
    @ApiModelProperty(value = "查询模式：0 查最近N条，1 查refMsgId之前N条")
    private int mode;

    @NotNull
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 10, message = "页大小不能小于10")
    @ApiModelProperty(value = "每次拉取的消息数量")
    private int pageSize;

    @NotNull
    @ApiModelProperty(value = "指定参考的msgId，不指定传-1")
    private long refMsgId;

}
