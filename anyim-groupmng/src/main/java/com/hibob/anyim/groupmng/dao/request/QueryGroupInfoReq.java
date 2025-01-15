package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("查询群组信息请求的参数")
public class QueryGroupInfoReq {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

}
