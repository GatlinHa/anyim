package com.hibob.anyim.chat.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("更新会话记录的一些信息接口的请求体参数")
public class UpdateSessionReq {

    @NotEmpty
    @ApiModelProperty(value = "会话Id")
    private String sessionId;

    @ApiModelProperty(value = "是否置顶")
    private Boolean top;

    @ApiModelProperty(value = "是否免打扰")
    private Boolean dnd;

    @ApiModelProperty(value = "草稿")
    private String draft;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "分组id")
    private Integer partitionId;
}
