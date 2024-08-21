package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@ApiModel("登录请求的参数")
public class LoginReq extends BaseRequest {

    @NotEmpty(message = "账号不可为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,32}$", message = "账号长度必须是6-32位的字母、数字或下划线")
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
