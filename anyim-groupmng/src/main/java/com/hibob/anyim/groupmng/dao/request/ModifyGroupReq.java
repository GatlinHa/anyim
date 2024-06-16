package com.hibob.anyim.groupmng.dao.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel("修改群组请求的参数")
public class ModifyGroupReq extends BaseRequest {

    @ApiModelProperty(value = "群组id")
    @NotNull
    private long groupId;

    @ApiModelProperty(value = "可选参数，群组名称")
    @Size(max = 255, message = "群组名称长度不能大于255字符")
    private String groupName;

    @ApiModelProperty(value = "可选参数，群组公告")
    @Size(max = 1024, message = "群组公告长度不能大于1024字符")
    private String announcement;

    @ApiModelProperty(value = "可选参数，群组头像")
    @Size(max = 255, message = "群组头像长度不能大于255字符")
    private String avatar;

}
