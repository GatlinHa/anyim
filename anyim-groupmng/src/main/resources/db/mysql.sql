use `anyim`;
DROP TABLE `anyim_group_info`;
CREATE TABLE `anyim_group_info`(
    `group_id` VARCHAR(255) NOT NULL COMMENT '群组ID，雪花算法生成',
    `group_type`  TINYINT(1) NOT NULL COMMENT '群类型：1普通群，2其他',
    `group_name` VARCHAR(255) DEFAULT '' COMMENT '名称',
    `announcement` VARCHAR(1024) DEFAULT '' COMMENT '公告',
    `avatar` VARCHAR(1024) DEFAULT '' COMMENT '群组头像',
    `avatar_thumb` VARCHAR(1024) DEFAULT '' COMMENT '群组头像缩略图',
    `history_browse` BOOLEAN DEFAULT FALSE COMMENT '是否新成员可查看历史消息',
    `all_muted` BOOLEAN DEFAULT FALSE COMMENT '是否全员禁言,默认false',
    `join_group_approval` BOOLEAN DEFAULT FALSE COMMENT '是否入群验证,默认false',
    `creator` VARCHAR(255) NOT NULL COMMENT '创建者账号',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `del_flag` BOOLEAN DEFAULT FALSE COMMENT '软删除的标记',
    `del_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (group_id),
    INDEX `idx_creator`(creator)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组信息表';

DROP TABLE `anyim_group_member`;
CREATE TABLE `anyim_group_member`(
    `group_id` VARCHAR(255) NOT NULL COMMENT '群组ID，雪花算法生成',
    `account` VARCHAR(255) NOT NULL COMMENT '成员账号',
    `nick_name` VARCHAR(255) NOT NULL COMMENT '群内昵称',
    `role` TINYINT(1) DEFAULT 0 COMMENT '成员角色：0普通成员，1管理员，2群主',
    `muted_mode` TINYINT(1) DEFAULT 0 COMMENT '禁言模式: 0未设置, 1禁言黑名单, 2禁言白名单',
    PRIMARY KEY (group_id, account)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组成员表';

