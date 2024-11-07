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

    @TableField(value = "member_account")
    private String memberAccount;

    @TableField(value = "member_nick_name")
    private String memberNickName;

    @TableField(value = "member_role")
    private int memberRole;

    @TableField(value = "muted")
    private boolean muted;
}
