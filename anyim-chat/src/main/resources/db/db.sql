use `anyim`;
drop table anyim_chat_refmsgid;
CREATE TABLE anyim_chat_refmsgid
(
    `session_id` BIGINT NOT NULL PRIMARY KEY  COMMENT '会话id，雪花算法生成，不会重复',
    `session_type` TINYINT(1) NOT NULL COMMENT '会话类型，0:单聊，1:群聊',
    `ref_msg_id` INT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY(`session_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT 'msgId参考值表'; -- TODO 这张表在创建session时插入数据

-- 创建一个新的单聊会话，就会插入2条记录；创建一个群组，根据初始成员数量插入对应的条数
DROP TABLE `anyim_chat_sessions`;
CREATE TABLE `anyim_chat_sessions`(
    `account` VARCHAR(255) NOT NULL COMMENT '账号',
    `session_type` TINYINT(1) NOT NULL COMMENT '会话类型，0:单聊，1:群聊', -- TODO 应该是非必要信息，不参与业务逻辑
    `session_id` BIGINT NOT NULL COMMENT 'session id，当为群聊时，就是group_id',
    `new_msg_id` INT DEFAULT 0 COMMENT '最新的消息id，客户端以new_msg_id是否大于本地已读msg_id判断是否需要更新消息，消息入库时同步要写这个字段',
    `new_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最新消息的时间',
    INDEX `idx_account`(ACCOUNT)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '账号下的会话列表';

drop table `anyim_chat_msg_chat`;
CREATE TABLE `anyim_chat_msg_chat`(
    `session_id` bigint NOT NULL COMMENT '会话ID，雪花算法生成',
    `from_id` VARCHAR(255) NOT NULL COMMENT '发送端账号',
    `from_client` VARCHAR(255) NOT NULL COMMENT '发送端客户端',
    `to_id` VARCHAR(255) NOT NULL COMMENT '接收端账号',
    `msg_id` INT NOT NULL COMMENT '消息id',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `send_time` DATETIME NOT NULL COMMENT '消息发送时间，取自netty接收到的时间',
    `creat_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消息入库时间',
    INDEX `idx_session_id`(session_id),
    INDEX `idx_msg_id`(msg_id) -- TODO 先不加send_time的索引
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '单聊消息记录表';

DROP TABLE `anyim_chat_msg_groupchat`;
CREATE TABLE `anyim_chat_msg_groupchat`(
    `group_id` BIGINT NOT NULL COMMENT '组ID，雪花算法生成，等于session id',
    `from_id` VARCHAR(255) NOT NULL COMMENT '发送端账号',
    `from_client` VARCHAR(255) NOT NULL COMMENT '发送端客户端',
    `msg_id` INT NOT NULL COMMENT '消息id',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `send_time` DATETIME NOT NULL COMMENT '消息发送时间，取自netty接收到的时间',
    `creat_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消息入库时间',
    INDEX `idx_group_id`(group_id),
    INDEX `idx_msg_id`(msg_id) -- TODO 先不加send_time的索引
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群聊消息记录表';

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
