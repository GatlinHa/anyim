package com.hibob.anyim.chat.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("删除分组请求的参数")
public class PartitionDelReq {

    @NotNull(message = "分组id不能为空")
    @ApiModelProperty(value = "分组id")
    private int partitionId;

}
