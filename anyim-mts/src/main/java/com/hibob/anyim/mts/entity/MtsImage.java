package com.hibob.anyim.mts.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_mts_image")
public class MtsImage {
    private static final long serialVersionUID = 1L;

    @TableId(value = "image_id")
    private String imageId;

    @TableField(value = "image_type")
    private String imageType;

    @TableField(value = "image_size")
    private long imageSize;

    @TableField(value = "origin_url")
    private String originUrl;

    @TableField(value = "thumb_url")
    private String thumbUrl;

    @TableField(value = "expire")
    private long expire;

    @TableField(value = "created_account")
    private String createdAccount;

    @TableField(value = "created_time")
    private Date createdTime;
}
