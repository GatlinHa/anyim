package com.hibob.anyim.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("修改密码请求的参数")
public class ModifyPwdReq {

    @NotEmpty(message = "旧密码不可为空")
    @ApiModelProperty(value = "旧密码")
    private String oldPassword;

    @NotEmpty(message = "新密码不可为空")
    @ApiModelProperty(value = "新密码")
    private String password;

}
