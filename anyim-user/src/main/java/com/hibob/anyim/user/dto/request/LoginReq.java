package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("登录请求的参数")
public class LoginReq extends BaseRequest {

    @Size(max = 64, message = "账号长度不能大于64字符")
    @NotEmpty(message = "账号不可为空")
    @ApiModelProperty(value = "账号")
    private String account;

    @Size(max = 128, message = "客户端ID长度不能大于128字符")
    @NotEmpty(message = "客户端ID不可为空")
    @ApiModelProperty(value = "客户端ID")
    private String clientId;

    @NotEmpty(message = "密码不可为空")
    @ApiModelProperty(value = "密码")
    private String password;

}
