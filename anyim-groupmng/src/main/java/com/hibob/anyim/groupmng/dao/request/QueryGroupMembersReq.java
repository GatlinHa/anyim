package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("查询群组成员请求的参数")
public class QueryGroupMembersReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

}
