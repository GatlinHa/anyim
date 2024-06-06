package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anyim_chat_msg_groupchat")
public class MsgGroupChat {
    private static final long serialVersionUID = 1L;

    @TableId(value = "group_id")
    private long groupId;

    @TableField(value = "from_id")
    private String fromId;

    @TableField(value = "from_client")
    private String fromClient;

    @TableField(value = "msg_id")
    private long msgId;

    @TableField(value = "msg_type")
    private int msgType;

    @TableField(value = "content")
    private String content;

    @TableField(value = "msg_time")
    private Date msgTime;
}
