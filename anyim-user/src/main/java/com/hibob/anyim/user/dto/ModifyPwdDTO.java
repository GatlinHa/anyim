package com.hibob.anyim.user.dto;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("修改密码请求的参数")
public class ModifyPwdDTO extends BaseRequest {

//    @NotEmpty(message = "昵称不可为空")
//    @ApiModelProperty(value = "昵称")
//    private String nickname;
//
//    @NotEmpty(message = "密码不可为空")
//    @ApiModelProperty(value = "密码")
//    private String password;
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
