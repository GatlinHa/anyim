package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ApiModel("创建单聊的参数")
public class NewChatReq extends BaseRequest {

    @Size(max = 64, message = "对方账号长度不能大于64字符")
    @NotEmpty(message = "对方账号不可为空")
    @ApiModelProperty(value = "对方账号")
    private String toAccountId;
}
