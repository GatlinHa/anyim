package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("删除分组请求的参数")
public class PartitionDelReq extends BaseRequest {

    @NotNull(message = "分组id不能为空")
    @ApiModelProperty(value = "分组id")
    private int partitionId;

}
