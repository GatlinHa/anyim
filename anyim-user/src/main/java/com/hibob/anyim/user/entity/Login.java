package com.hibob.anyim.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_user_login")
public class Login {
    private static final long serialVersionUID = 1L;

    @TableField(value = "account")
    private String account;

    @TableField(value = "unique_id")
    private String uniqueId;

    @TableField("login_time")
    private Date loginTime;

    @TableField("refresh_time")
    private Date refreshTime;

    @TableField("logout_time")
    private Date logoutTime;

}
