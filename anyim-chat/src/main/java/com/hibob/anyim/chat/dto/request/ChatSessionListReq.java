package com.hibob.anyim.chat.dto.request;

import com.hibob.anyim.common.model.BaseRequest;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("查询单聊会话列表接口的请求体参数")
public class ChatSessionListReq extends BaseRequest {

}
