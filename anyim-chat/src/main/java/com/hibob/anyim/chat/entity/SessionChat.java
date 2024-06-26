package com.hibob.anyim.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anyim_chat_session_chat")
public class SessionChat {
    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id")
    private String sessionId;

    @TableField(value = "user_a")
    private String userA;

    @TableField(value = "user_b")
    private String userB;

    @TableField(value = "ref_msg_id")
    private long refMsgId;

}
