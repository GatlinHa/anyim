package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_chat_refmsgid")
public class RefMsgId {
    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id")
    private long sessionId;

    @TableField(value = "session_type")
    private byte sessionType;

    @TableField(value = "ref_msg_id")
    private int refMsgId;

    @TableField(value = "update_time")
    private Date updateTime;
}
