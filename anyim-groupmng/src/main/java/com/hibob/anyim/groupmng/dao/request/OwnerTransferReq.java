package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("群主转让请求的参数")
public class OwnerTransferReq {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "新群主账号")
    @NotEmpty
    private String account;
}
