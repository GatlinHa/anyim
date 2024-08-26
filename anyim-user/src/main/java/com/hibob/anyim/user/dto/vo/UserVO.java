package com.hibob.anyim.user.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询别人信息返回的参数")
public class UserVO {

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "头像缩略图")
    private String avatarThumb;

    @ApiModelProperty(value = "性别")
    private int sex;

    @ApiModelProperty(value = "级别")
    private int level;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "手机号码")
    private String phoneNum;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "生日")
    private String birthday;

}
