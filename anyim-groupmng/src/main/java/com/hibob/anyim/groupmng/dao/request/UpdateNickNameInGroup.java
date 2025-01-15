package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("修改自己的群昵称")
public class UpdateNickNameInGroup {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "群昵称")
    @NotEmpty
    @Size(max = 255, message = "群昵称长度不能大于255字符")
    private String nickName;
}
