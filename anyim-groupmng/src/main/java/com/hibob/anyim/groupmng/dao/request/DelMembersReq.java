package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("群组减人请求的参数")
public class DelMembersReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "减少的群组成员：account数组")
    @NotNull
    private List<String> accounts;
}
