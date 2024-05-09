use `anyim`;
drop table `anyim_user`;
CREATE TABLE `anyim_user`(
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