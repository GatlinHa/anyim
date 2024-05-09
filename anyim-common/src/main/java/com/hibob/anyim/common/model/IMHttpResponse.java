package com.hibob.anyim.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Http短连接请求统一响应格式")
public class IMHttpResponse {

    @ApiModelProperty(value = "返回码，0表示成功，其它表示失败")
    private Integer code;

    @ApiModelProperty(value = "返回码描述")
    private String desc;

    @ApiModelProperty(value = "返回数据")
    private Object data;
}
