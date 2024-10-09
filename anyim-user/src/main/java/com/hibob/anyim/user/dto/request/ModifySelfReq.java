package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("修改自己信息请求的参数")
public class ModifySelfReq extends BaseRequest {

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "头像缩略图")
    private String avatarThumb;

    @ApiModelProperty(value = "性别")
    private int gender;

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
