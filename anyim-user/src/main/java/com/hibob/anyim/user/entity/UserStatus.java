package com.hibob.anyim.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_user_status")
public class UserStatus extends Model<UserStatus> {

    private static final long serialVersionUID = 1L;

    @TableField(value = "account")
    private String account;

    @TableField("unique_id")
    private String uniqueId;

    @TableField("status")
    private int status;

    @TableField("update_time")
    private Date updateTime;
}
