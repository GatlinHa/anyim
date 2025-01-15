package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("修改成员角色请求的参数")
public class ChangeRoleReq {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "待修改的成员")
    @NotEmpty
    private String account;

    @ApiModelProperty(value = "角色")
    @NotNull
    @Max(value = 2, message = "角色类型不能大于2")
    @Min(value = 0, message = "角色类型不能小于0")
    private int role;
}
