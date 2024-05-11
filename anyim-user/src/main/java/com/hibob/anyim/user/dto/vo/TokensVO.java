package com.hibob.anyim.user.dto.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录返回的参数")
public class TokensVO {
    @ApiModelProperty(value = "每次请求都必须在header中携带accessToken")
    private String accessToken;

    @ApiModelProperty(value = "accessToken过期时间(秒)")
    private Integer accessTokenExpires;

    @ApiModelProperty(value = "accessToken过期后，通过refreshToken换取新的token")
    private String refreshToken;

    @ApiModelProperty(value = "refreshToken过期时间(秒)")
    private Integer refreshTokenExpires;
}
