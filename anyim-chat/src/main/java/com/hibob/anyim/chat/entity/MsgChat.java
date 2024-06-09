package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_chat_msg_chat")
public class MsgChat {
    private static final long serialVersionUID = 1L;

    @TableField(value = "session_id")
    private String sessionId;

    @TableField(value = "from_id")
    private String fromId;

    @TableField(value = "from_client")
    private String fromClient;

    @TableField(value = "to_id")
    private String toId;

    @TableField(value = "msg_id")
    private long msgId;

    @TableField(value = "msg_type")
    private int msgType;

    @TableField(value = "content")
    private String content;

    @TableField(value = "msg_time")
    private Date msgTime;

    @TableField(value = "create_time")
    private Date createTime;
}
