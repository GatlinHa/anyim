use `anyim`;
DROP TABLE `anyim_group_info`;
CREATE TABLE `anyim_group_info`(
    `group_id` VARCHAR(255) NOT NULL COMMENT '群组ID，雪花算法生成',
    `group_type`  TINYINT(1) NOT NULL COMMENT '群类型：1普通群，2其他',
    `group_name` VARCHAR(255) DEFAULT '' COMMENT '名称',
    `announcement` VARCHAR(1024) DEFAULT '' COMMENT '公告',
    `avatar` VARCHAR(255) DEFAULT '' COMMENT '群组头像',
    `avatar_thumb` VARCHAR(255) DEFAULT '' COMMENT '群组头像缩略图',
    `history_browse` BOOLEAN DEFAULT FALSE COMMENT '是否新成员可查看历史消息',
    `muted` BOOLEAN DEFAULT FALSE COMMENT '是否全员禁言',
    PRIMARY KEY (group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组信息表';

DROP TABLE `anyim_group_member`;
CREATE TABLE `anyim_group_member`(
    `group_id` VARCHAR(255) NOT NULL COMMENT '群组ID，雪花算法生成',
    `member_account` VARCHAR(255) NOT NULL COMMENT '成员账号',
    `member_nick_name` VARCHAR(255) NOT NULL COMMENT '成员昵称',
    `member_role` TINYINT(1) DEFAULT 0 COMMENT '成员角色：0普通成员，1管理员，2群主',
    `muted` BOOLEAN DEFAULT FALSE COMMENT '是否被禁言',
    PRIMARY KEY (group_id, member_account)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组成员表';

