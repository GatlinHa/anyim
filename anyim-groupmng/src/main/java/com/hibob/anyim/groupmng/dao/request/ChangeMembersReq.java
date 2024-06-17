package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("群组加/减人请求的参数")
public class ChangeMembersReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotNull
    private long groupId;

    @ApiModelProperty(value = "增加的群组成员：memberAccount, memberRole")
    @NotNull
    private List<Map<String, Object>> addMembers;

    @ApiModelProperty(value = "减少的群组成员：memberAccount, memberRole")
    @NotNull
    private List<Map<String, Object>> delMembers;
}
