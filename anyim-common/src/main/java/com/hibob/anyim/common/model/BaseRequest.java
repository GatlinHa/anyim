package com.hibob.anyim.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel("Http短连接请求公共参数")
public class BaseRequest {

    @Size(max = 255, message = "账号长度不能大于255字符")
    @ApiModelProperty(value = "accessToken")
    private String accessToken;

    //TODO 参数太多，影响测试效率，先注掉
//    @ApiModelProperty(value = "设备ID")
//    private Long deviceId;
//
//    @Max(value = 2, message = "设备类型取值范围:0,2")
//    @Min(value = 0, message = "设备类型取值范围:0,2")
//    @ApiModelProperty(value = "设备类型")
//    private int deviceType;
//
//    @Max(value = 5, message = "客户端类型取值范围:0,5")
//    @Min(value = 0, message = "客户端类型取值范围:0,5")
//    @ApiModelProperty(value = "客户端类型")
//    private String clientType;
//
//    @Size(max = 10, message = "账号长度不能大于10字符")
//    @ApiModelProperty(value = "客户端版本")
//    private String clientVersion;

}
