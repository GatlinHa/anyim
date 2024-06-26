package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anyim_chat_session_groupchat")
public class SessionGroupChat {
    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id")
    private String sessionId;

    @TableField(value = "group_id")
    private long groupId;

    @TableField(value = "ref_msg_id")
    private long refMsgId;

}
