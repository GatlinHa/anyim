package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO 待迁移至MTS
 */
@Data
@ApiModel("文件上传请求")
public class UploadReq extends BaseRequest {

    @ApiModelProperty(value = "文件")
    private MultipartFile file;
}
