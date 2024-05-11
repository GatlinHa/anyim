package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询别人信息请求的参数")
public class QueryReq extends BaseRequest {

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @ApiModelProperty(value = "性别")
    private int sex;

    @ApiModelProperty(value = "级别")
    private int level;

    @ApiModelProperty(value = "个性签名")
    private String signature;

}
