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

    @ApiModelProperty(value = "群组类型")
    @NotNull
    @Max(value = 10, message = "群组类型不能大于10")
    @Min(value = 0, message = "群组类型不能小于0")
    private int groupType;

    @ApiModelProperty(value = "群组名称")
    @NotEmpty
    @Size(max = 255, message = "群组名称长度不能大于255字符")
    private String groupName;

    @ApiModelProperty(value = "可选参数，群组公告")
    @Size(max = 1024, message = "群组公告长度不能大于1024字符")
    private String announcement;

    @ApiModelProperty(value = "可选参数，群组头像")
    @Size(max = 255, message = "群组头像长度不能大于255字符")
    private String avatar;

    @ApiModelProperty(value = "群组成员：memberAccount, memberRole")
    @NotNull
    @Size(min = 1, message = "群组成员不能为空")
    private List<Map<String, Object>> members;
}
