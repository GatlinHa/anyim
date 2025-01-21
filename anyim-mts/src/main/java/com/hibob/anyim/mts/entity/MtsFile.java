package com.hibob.anyim.mts.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_mts_file")
public class MtsFile {
    private static final long serialVersionUID = 1L;

    @TableId(value = "file_id")
    private String fileId;

    @TableField(value = "file_type")
    private String fileType;

    @TableField(value = "file_size")
    private long fileSize;

    @TableField(value = "file_url")
    private String fileUrl;

    @TableField(value = "expire")
    private long expire;

    @TableField(value = "created_account")
    private String createdAccount;

    @TableField(value = "created_time")
    private Date createdTime;
}
