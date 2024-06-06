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
    private String groupId;

    @TableField(value = "from_id")
    private String fromId;

    @TableField(value = "from_client")
    private String fromClient;

    @TableField(value = "msg_id")
    private long msgId;

    @TableField(value = "content")
    private String content;

    @TableField(value = "send_time")
    private Date sendTime;

    @TableField(value = "creat_time")
    private Date creatTime;
}
