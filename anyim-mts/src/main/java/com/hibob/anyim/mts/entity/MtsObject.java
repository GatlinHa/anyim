package com.hibob.anyim.mts.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_mts_object")
public class MtsObject {
    private static final long serialVersionUID = 1L;

    @TableId(value = "object_id")
    private String objectId;

    @TableField(value = "object_name")
    private String objectName;

    @TableField(value = "object_type")
    private String objectType;

    @TableField(value = "object_size")
    private long objectSize;

    @TableField(value = "origin_url")
    private String originUrl;

    @TableField(value = "thumb_url")
    private String thumbUrl;

    @TableField(value = "expire")
    private long expire;

    @TableField(value = "created_time")
    private Date createdTime;
}
