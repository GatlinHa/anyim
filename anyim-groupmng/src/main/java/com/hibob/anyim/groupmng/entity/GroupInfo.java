package com.hibob.anyim.groupmng.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("anyim_group_info")
public class GroupInfo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "group_id")
    private String groupId;

    @TableField(value = "group_type")
    private Integer groupType;

    @TableField(value = "group_name")
    private String groupName;

    @TableField(value = "announcement")
    private String announcement;

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "avatar_thumb")
    private String avatarThumb;

    @TableField(value = "history_browse")
    private boolean historyBrowse;

    @TableField(value = "all_muted")
    private boolean allMuted;

    @TableField(value = "all_invite")
    private boolean allInvite;

    @TableField(value = "creator")
    private String creator;
}
