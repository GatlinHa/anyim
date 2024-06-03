package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_chat_sessions")
public class Sessions {
    private static final long serialVersionUID = 1L;

    @TableField(value = "account")
    private String account;

    @TableField(value = "session_type")
    private byte sessionType;

    @TableField(value = "session_id")
    private long sessionId;

    @TableField(value = "new_msg_id")
    private int newMsgId;

    @TableField(value = "new_time")
    private Date newTime;
}
