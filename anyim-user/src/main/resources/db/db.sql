use `anyim`;
drop table `anyim_user_info`;
CREATE TABLE `anyim_user_info`(
    `id` BIGINT NOT NULL PRIMARY KEY  COMMENT 'id，雪花算法生成',
    `account` VARCHAR(255) NOT NULL COMMENT '账号，用户不指定就生成UUID',
    `nick_name` VARCHAR(255) NOT NULL COMMENT '用户昵称',
    `head_image` VARCHAR(255) DEFAULT '' COMMENT '用户头像',
    `head_image_thumb` VARCHAR(255) DEFAULT '' COMMENT '用户头像缩略图',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `sex`  TINYINT(1) DEFAULT 0 COMMENT '性别 0:男 1:女',
    `level`  SMALLINT DEFAULT 1 COMMENT '用户级别 0:普通用户 其他:TODO',
    `signature` VARCHAR(1024) DEFAULT '' COMMENT '个性签名',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    KEY `idx_id`(id),
    INDEX `idx_account`(account),
    INDEX `idx_nick_name`(nick_name)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '用户信息表';

drop table `anyim_user_client`;
CREATE TABLE `anyim_user_client`(
    `unique_id` VARCHAR(255) NOT NULL COMMENT '客户端唯一ID，account+|+客户端生成的uuid',
    `account` VARCHAR(255) NOT NULL COMMENT '账号',
    `client_type` TINYINT(1) DEFAULT -1 COMMENT '客户端类型 0:android 1:ios 2:web -1:未知',
    `client_name` VARCHAR(255) DEFAULT '' COMMENT '客户端名称',
    `client_version` VARCHAR(255) DEFAULT '' COMMENT '客户端版本',
    `last_login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY(`unique_id`),
    INDEX `idx_account`(account)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '客户端表';

-- TODO 需要做老化处理
drop table `anyim_user_login`;
CREATE TABLE `anyim_user_login`(
    `account` VARCHAR(255) NOT NULL COMMENT '账号',
    `unique_id` VARCHAR(255) NOT NULL COMMENT '客户端唯一ID，account+|+客户端生成的uuid',
    `login_time` DATETIME DEFAULT NULL COMMENT '登录时间',
    `refresh_time` DATETIME DEFAULT NULL COMMENT '刷新token时间',
    `logout_time` DATETIME DEFAULT NULL COMMENT '登出时间',
    INDEX `idx_account`(account)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '登录记录表';