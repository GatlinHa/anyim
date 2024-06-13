use `anyim`;
DROP TABLE `anyim_chat_session_chat`;
CREATE TABLE `anyim_chat_session_chat`
(
    `session_id` VARCHAR(512) NOT NULL COMMENT 'session id，主键，user_a@user_b，其中user_a的排序要小于user_b，ASCII排序',
    `user_a` VARCHAR(255) NOT NULL COMMENT '单聊用户a，ASCII排序较小者',
    `user_b` VARCHAR(255) NOT NULL COMMENT '单聊用户b，ASCII排序较大者',
    `ref_msg_id` BIGINT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    PRIMARY KEY(`session_id`),
    INDEX `idx_user_a`(user_a),
    INDEX `idx_user_b`(user_b)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '单聊的会话表';

DROP TABLE `anyim_chat_session_groupchat`;
CREATE TABLE `anyim_chat_session_groupchat`(
    `session_id` VARCHAR(512) NOT NULL COMMENT 'session id，当前实现等于group id，主键',
    `group_id` BIGINT NOT NULL COMMENT '组ID',
    `ref_msg_id` BIGINT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    PRIMARY KEY(`session_id`),
    INDEX `idx_group_id`(group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群聊的会话表';

-- TODO 这两个是群组表 先暂时放到这里
DROP TABLE `anyim_group_member`;
CREATE TABLE `anyim_group_member`(
    `group_id` BIGINT NOT NULL COMMENT '组ID，雪花算法生成',
    `member` VARCHAR(255) NOT NULL COMMENT '成员账号',
    INDEX `idx_group_id`(group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组成员表';

DROP TABLE `anyim_group_info`;
CREATE TABLE `anyim_group_info`(
    `group_id` BIGINT NOT NULL COMMENT '组ID，雪花算法生成',
    `name` VARCHAR(255) NOT NULL COMMENT '名称',
    `notice` VARCHAR(255) NOT NULL COMMENT '公告',
    `manager` JSON NOT NULL COMMENT '管理员',
    INDEX `idx_group_id`(group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组信息表';
