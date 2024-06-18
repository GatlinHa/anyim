package com.hibob.anyim.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("REST短连接请求公共参数")
public class BaseRequest {

    @NotNull
    @Max(value = 10, message = "客户端类型取值范围:0,10")
    @Min(value = 0, message = "客户端类型取值范围:0,10")
    @ApiModelProperty(value = "客户端类型")
    private int clientType;

    @NotEmpty
    @Size(max = 255, message = "客户端名称长度不能大于255字符")
    @ApiModelProperty(value = "客户端名称")
    private String clientName;

    @NotEmpty
    @Size(max = 255, message = "客户端名称长度不能大于255字符")
    @ApiModelProperty(value = "客户端版本")
    private String clientVersion;

}
