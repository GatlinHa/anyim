package com.hibob.anyim.user.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("查询自己信息请求的参数")
public class QuerySelfReq extends BaseRequest {

}
