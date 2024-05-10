package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("刷新token请求的参数")
public class RefreshTokenReq extends BaseRequest {

    @Size(max = 255, message = "refreshToken长度不能大于255字符")
    @NotEmpty(message = "refreshToken不可为空")
    @ApiModelProperty(value = "refreshToken")
    private String refreshToken;

}
