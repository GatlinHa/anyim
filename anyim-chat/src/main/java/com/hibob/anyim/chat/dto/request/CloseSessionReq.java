package com.hibob.anyim.chat.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("关闭会话记录接口的请求体参数")
public class CloseSessionReq {

    @NotEmpty
    @ApiModelProperty(value = "会话Id")
    private String sessionId;
}
