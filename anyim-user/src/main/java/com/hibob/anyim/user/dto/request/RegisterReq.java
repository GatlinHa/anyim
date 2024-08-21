package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
@ApiModel("注册请求的参数")
public class RegisterReq extends BaseRequest {

    @NotEmpty(message = "账号不可为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,32}$", message = "账号必须是6-32位的字母、数字或下划线")
    @ApiModelProperty(value = "账号")
    private String account;

    @NotEmpty(message = "密码不可为空")
    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickName;

//    @ApiModelProperty(value = "头像")
//    private String avatar;
//
//    @ApiModelProperty(value = "电话号码")
//    private String phoneNum;
//
//    @ApiModelProperty(value = "邀请码")
//    private String inviteCode;

}
