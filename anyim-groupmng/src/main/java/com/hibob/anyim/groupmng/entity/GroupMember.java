package com.hibob.anyim.groupmng.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anyim_group_member")
public class GroupMember {
    private static final long serialVersionUID = 1L;

    @TableField(value = "group_id")
    private String groupId;

    @TableField(value = "account")
    private String account;

    @TableField(value = "nick_name")
    private String nickName;

    @TableField(value = "avatar_thumb")
    private String avatarThumb;

    @TableField(value = "role")
    private int role;

    @TableField(value = "muted")
    private boolean muted;
}
