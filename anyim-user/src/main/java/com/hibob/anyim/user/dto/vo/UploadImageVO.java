package com.hibob.anyim.user.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * TODO 待迁移至MTS
 */
@Data
@ApiModel("上传文件后返回的参数")
public class UploadImageVO {
    @ApiModelProperty(value = "原图")
    private String originUrl;

    @ApiModelProperty(value = "缩略图")
    private String thumbUrl;
}
