package com.hibob.anyim.groupmng.dao.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("根据成员群昵称或账号搜索群id")
public class SearchGroupMemberReq {

    @ApiModelProperty(value = "搜索关键词")
    @NotEmpty
    private String searchKey;

}
