package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("解散群组请求的参数")
public class DropGroupReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotNull
    private String groupId;

    @ApiModelProperty(value = "群解散时的最后一个msgId")
    @NotNull
    private long leaveMsgId;

}
