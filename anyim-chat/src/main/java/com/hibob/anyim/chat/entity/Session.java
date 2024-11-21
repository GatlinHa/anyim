package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

@Data
@TableName("anyim_chat_session")
public class Session {
    private static final long serialVersionUID = 1L;

    @TableField(value = "account")
    @Indexed
    private String account;

    @TableField(value = "session_id")
    @Indexed
    private String sessionId;

    @TableField(value = "remote_id")
    private String remoteId;

    @TableField(value = "session_type")
    private int sessionType;

    @TableField(value = "read_msg_id")
    private long readMsgId;

    @TableField(value = "read_time")
    private Date readTime;

    @TableField(value = "top")
    private boolean top;

    @TableField(value = "dnd")
    private boolean dnd;

    @TableField(value = "draft")
    private String draft;

    @TableField(value = "mark")
    private String mark;

    @TableField(value = "partition_id")
    private int partitionId;

    @TableField(value = "del_flag")
    private Boolean delFlag;

    @TableField(value = "remote_read", exist = false)
    private long remoteRead;
}
