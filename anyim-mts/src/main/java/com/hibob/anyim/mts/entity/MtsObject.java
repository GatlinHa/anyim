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

    @TableId(value = "obj_id")
    private String objId;

    @TableField(value = "obj_name")
    private String objName;

    @TableField(value = "obj_type")
    private String objType;

    @TableField(value = "obj_size")
    private long objSize;

    @TableField(value = "ori_url")
    private String oriUrl;

    @TableField(value = "thumb_url")
    private String thumbUrl;

    @TableField(value = "expire")
    private long expire;

    @TableField(value = "created_time")
    private Date createdTime;
}
