package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_chat_partition")
public class Partition extends Model<Partition> {

    private static final long serialVersionUID = 1L;

    @TableField(value = "account")
    private String account;

    @TableField("partition_id")
    private int partitionId;

    @TableField("partition_name")
    private String partitionName;

    @TableField("partition_type")
    private int partitionType;

    @TableField("update_time")
    private Date updateTime;
}
