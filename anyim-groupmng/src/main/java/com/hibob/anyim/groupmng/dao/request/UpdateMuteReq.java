package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("修改自己的群昵称")
public class UpdateMuteReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotEmpty
    private String groupId;

    @ApiModelProperty(value = "操作对象的账号")
    @NotEmpty
    private String account;

    @ApiModelProperty(value = "禁言模式")
    @NotNull
    @Min(value = 0, message = "禁言模式取值范围:0,1,2")
    @Max(value = 2, message = "禁言模式取值范围:0,1,2")
    private int mutedMode;
}
