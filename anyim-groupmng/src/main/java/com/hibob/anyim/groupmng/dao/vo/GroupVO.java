package com.hibob.anyim.groupmng.dao.vo;

import com.hibob.anyim.groupmng.entity.GroupInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel("群组信息返回的参数")
public class GroupVO {

    @ApiModelProperty(value = "群组信息")
    private GroupInfo groupInfo;

    /**
     * 群组成员, key是账号,value是info对象
     * tips: 这里要把已退出成员也查到，为了已退出成员之前发的消息的展示
     */
    @ApiModelProperty(value = "群组成员, key是账号,value是info对象")
    private Map<String, Map<String, Object>> members;
}
