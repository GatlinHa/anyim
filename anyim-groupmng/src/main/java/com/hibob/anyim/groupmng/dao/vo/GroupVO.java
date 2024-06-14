package com.hibob.anyim.groupmng.dao.vo;

import com.hibob.anyim.groupmng.entity.GroupInfo;
import com.hibob.anyim.groupmng.entity.GroupMember;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("群组信息返回的参数")
public class GroupVO {

    @ApiModelProperty(value = "群组信息")
    private GroupInfo groupInfo;

    @ApiModelProperty(value = "群组成员")
    private List<GroupMember> members;
}
