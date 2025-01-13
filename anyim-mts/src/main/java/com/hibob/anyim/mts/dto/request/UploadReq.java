package com.hibob.anyim.mts.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@ApiModel("文件上传请求")
public class UploadReq extends BaseRequest {

    @ApiModelProperty(value = "文件")
    private MultipartFile file;
}
