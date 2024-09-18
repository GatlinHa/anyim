package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
@ApiModel("更新会话记录的一些信息接口的请求体参数")
public class UpdateSessionReq extends BaseRequest {

    @NotEmpty
    @ApiModelProperty(value = "会话Id")
    private String sessionId;

    @ApiModelProperty(value = "已读消息id")
    private Long readMsgId;

    @ApiModelProperty(value = "已读时间")
    private Date readTime;

    @ApiModelProperty(value = "是否置顶")
    private Boolean top;

    @ApiModelProperty(value = "是否静音")
    private Boolean muted;

    @ApiModelProperty(value = "草稿")
    private String draft;
}
