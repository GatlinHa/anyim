package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("查询群组信息请求的参数")
public class QueryGroupReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotNull
    private long groupId;

}
