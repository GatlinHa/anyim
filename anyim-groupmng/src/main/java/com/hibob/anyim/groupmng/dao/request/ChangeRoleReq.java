package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("修改成员角色请求的参数")
public class ChangeRoleReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "待修改的成员")
    @NotEmpty
    private String memberAccount;

    @ApiModelProperty(value = "角色")
    @NotNull
    private int memberRole;
}
