package com.hibob.anyim.chat.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("更新分组请求的参数")
public class PartitionUpdateReq {

    @NotNull(message = "分组id不能为空")
    @ApiModelProperty(value = "分组id")
    private int partitionId;

    @NotEmpty(message = "新的分组名字不能为空")
    @ApiModelProperty(value = "新的分组名字")
    private String newPartitionName;

}
