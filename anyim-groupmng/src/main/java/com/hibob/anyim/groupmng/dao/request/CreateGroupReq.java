package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("创建群组请求的参数")
public class CreateGroupReq extends BaseRequest {

    @ApiModelProperty(value = "群组名称")
    @NotEmpty(message = "群组名称不能为空")
    @Size(max = 50, message = "群组名称不能大于50字符")
    private String groupName;

    @ApiModelProperty(value = "群组类型")
    @NotNull
    @Max(value = 10, message = "群组类型不能大于10")
    @Min(value = 0, message = "群组类型不能小于0")
    private int groupType;

    @ApiModelProperty(value = "群组成员：account, nickName")
    @NotNull
    @Size(min = 2, message = "除了创建者，群组成员不能少于2人")
    private List<Map<String, Object>> members;
}
