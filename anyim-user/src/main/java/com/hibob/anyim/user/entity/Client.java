package com.hibob.anyim.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.util.Date;

@Data
@TableName("anyim_user_client")
public class Client extends Model<Client> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "unique_id")
    private String uniqueId;

    @TableField(value = "account")
    private String account;

    @TableField(value = "client_type")
    private int clientType;

    @TableField(value = "client_name")
    private String clientName;

    @TableField(value = "client_version")
    private String clientVersion;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @TableField("created_time")
    private Date createdTime;
}
