use `anyim`;
DROP TABLE `anyim_chat_session_chat`; #TODO 改个名字
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

DROP TABLE `anyim_chat_session_groupchat`; #TODO 改个名字
CREATE TABLE `anyim_chat_session_groupchat`(
    `session_id` VARCHAR(512) NOT NULL COMMENT 'session id，当前实现等于group id，主键',
    `group_id` BIGINT NOT NULL COMMENT '组ID',
    `ref_msg_id` BIGINT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    PRIMARY KEY(`session_id`),
    INDEX `idx_group_id`(group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群聊的会话表';

DROP TABLE `anyim_chat_session`;
CREATE TABLE `anyim_chat_session`
(
    `account` VARCHAR(255) NOT NULL COMMENT '账号，联合主键',
    `session_id` VARCHAR(512) NOT NULL COMMENT 'session id，联合主键',
    `remote_id` VARCHAR(255) NOT NULL COMMENT '会话的对方Id，单聊为对方账号，群聊为groupId',
    `session_type`  TINYINT(1) DEFAULT 99 COMMENT '会话类型：按照MsgType定义',
    `read_msg_id` BIGINT DEFAULT 0 COMMENT '已读消息Id，初始值0，表示没有已读的',
    `read_time` DATETIME DEFAULT '1970-01-01 00:00:00' COMMENT '已读消息的时间',
    `top` BOOLEAN DEFAULT FALSE COMMENT '会话是否置顶，默认false',
    `muted` BOOLEAN DEFAULT FALSE COMMENT '会话是否静音（免打扰），默认false',
    `draft` VARCHAR(3000) DEFAULT NULL COMMENT '草稿',
    PRIMARY KEY(`account`, `session_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '用户的会话信息表';