package com.hibob.anyim.user.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录返回的参数")
public class TokensVO {
    @ApiModelProperty(value = "每次请求都必须在header中携带token")
    private String token;

    @ApiModelProperty(value = "请求签名的密钥，过期时间与token相同")
    private String secret;

    @ApiModelProperty(value = "token过期时间(秒)")
    private Integer expire;

}
