use `anyim`;
-- 单聊：发第一条消息时，从redis取到的increment=0，因此要找chat要refmsgid，chat自己也查不到记录，就说明这个session是新的，需要先创建session相关的记录
-- 群聊：创建群聊的时候需要先选人，当执行创建按钮后，要给后台发REST请求，让后台显示地创建group和session
DROP TABLE `anyim_chat_session_chat`;
CREATE TABLE `anyim_chat_session_chat`(
    `session_id` BIGINT NOT NULL COMMENT 'session id，雪花算法生成，主键',
    `user_a` VARCHAR(255) NOT NULL COMMENT '单聊用户a，ASCII排序较小者',
    `user_b` VARCHAR(255) NOT NULL COMMENT '单聊用户b，ASCII排序较大者',
    `ref_msg_id` INT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    `new_msg_id` INT DEFAULT 0 COMMENT '最新的消息id，客户端以new_msg_id是否大于本地已读msg_id判断是否需要更新消息，消息入库时同步要写这个字段',
    `new_time` DATETIME DEFAULT NULL COMMENT '最新消息的时间',
    PRIMARY KEY(`session_id`),
    INDEX `idx_user_a`(user_a),
    INDEX `idx_user_b`(user_b),
    INDEX `idx_new_msg_id`(new_msg_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '单聊的会话列表';

-- 这个表是以session为视角设计的，方便按照session角度设计和实现代码
DROP TABLE `anyim_chat_session_groupchat`;
CREATE TABLE `anyim_chat_session_groupchat`(
    `session_id` BIGINT NOT NULL COMMENT 'session id，当前实现等于group id，主键',
    `group_id` BIGINT NOT NULL COMMENT '组ID',
    `ref_msg_id` INT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    `new_msg_id` INT DEFAULT 0 COMMENT '最新的消息id，客户端以new_msg_id是否大于本地已读msg_id判断是否需要更新消息，消息入库时同步要写这个字段',
    `new_time` DATETIME DEFAULT NULL COMMENT '最新消息的时间',
    PRIMARY KEY(`session_id`),
    INDEX `idx_group_id`(group_id),
    INDEX `idx_new_msg_id`(new_msg_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群聊的会话列表';


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
