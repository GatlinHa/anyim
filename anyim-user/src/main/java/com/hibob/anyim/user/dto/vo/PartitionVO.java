package com.hibob.anyim.user.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询分组信息返回的参数")
public class PartitionVO {

    @ApiModelProperty(value = "分组id")
    private int partitionId;

    @ApiModelProperty(value = "分组名称")
    private String partitionName;

    @ApiModelProperty(value = "分组类型")
    private int partitionType;

}
