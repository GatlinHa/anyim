package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("退出群组请求的参数")
public class LeaveGroupReq {
    @ApiModelProperty(value = "群组id")
    @NotNull
    private String groupId;

}
