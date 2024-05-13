package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("查询别人信息请求的参数")
public class QueryReq extends BaseRequest {

    @NotEmpty(message = "账号不可为空")
    @ApiModelProperty(value = "账号")
    private String account;

}
