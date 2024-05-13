package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("根据昵称找人请求的参数")
public class FindByNickReq extends BaseRequest {

    @NotEmpty(message = "昵称关键字不可为空")
    @ApiModelProperty(value = "昵称昵称关键字")
    private String nickNameKeyWords;
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
