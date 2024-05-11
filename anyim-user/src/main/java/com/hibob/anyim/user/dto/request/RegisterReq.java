package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
@ApiModel("注册请求的参数")
public class RegisterReq extends BaseRequest {

    @Size(max = 64, message = "账号长度不能大于64字符")
    @NotEmpty(message = "账号不可为空")
    @ApiModelProperty(value = "账号")
    private String account;

    @NotEmpty(message = "昵称不可为空")
    @ApiModelProperty(value = "昵称")
    private String nickName;

    @NotEmpty(message = "密码不可为空")
    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "电话号码")
    private String phoneNum;

    @ApiModelProperty(value = "邀请码")
    private String inviteCode;

}
