package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("群主转让请求的参数")
public class OwnerTransferReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotNull
    private long groupId;

    @ApiModelProperty(value = "新群主账号")
    @NotEmpty
    private String account;
}
