use `anyim`;
drop table anyim_chat_refmsgid;
CREATE TABLE anyim_chat_refmsgid(
    `session_id` BIGINT NOT NULL PRIMARY KEY  COMMENT '会话id，雪花算法生成，不会重复',
    `session_type` TINYINT(1) NOT NULL COMMENT '会话类型，0:单聊，1:群聊',
    `ref_msg_id` INT DEFAULT 10000 COMMENT 'msgId参考值，初始值10000',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY(`session_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT 'msgId参考值表，这张表在创建session时插入数据，按划分，应该归属与chat服务';

