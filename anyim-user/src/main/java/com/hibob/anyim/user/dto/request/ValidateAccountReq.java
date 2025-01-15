package com.hibob.anyim.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@ApiModel("登出请求的参数")
public class ValidateAccountReq {

    @NotEmpty(message = "账号不可为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,32}$", message = "账号必须是6-32位的字母、数字或下划线")
    @ApiModelProperty(value = "账号")
    private String account;

}
