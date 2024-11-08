package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel("查询群组信息请求的参数")
public class QueryGroupListReq extends BaseRequest {

    @ApiModelProperty(value = "查询角色")
    private List<Integer> roleList;
}
