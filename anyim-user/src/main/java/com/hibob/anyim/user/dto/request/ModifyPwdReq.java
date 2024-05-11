package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("修改密码请求的参数")
public class ModifyPwdReq extends BaseRequest {

//    @NotEmpty(message = "昵称不可为空")
//    @ApiModelProperty(value = "昵称")
//    private String nickname;
//
    @NotEmpty(message = "旧密码不可为空")
    @ApiModelProperty(value = "旧密码")
    private String oldPassword;

    @NotEmpty(message = "新密码不可为空")
    @ApiModelProperty(value = "新密码")
    private String password;
//
//    @ApiModelProperty(value = "头像")
//    private String avatar;
//
//    @ApiModelProperty(value = "电话号码")
//    private String phoneNum;
//
//    @ApiModelProperty(value = "邀请码")
//    private String inviteCode;
}
