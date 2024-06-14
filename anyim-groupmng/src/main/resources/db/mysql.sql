use `anyim`;
DROP TABLE `anyim_group_info`;
CREATE TABLE `anyim_group_info`(
    `group_id` BIGINT NOT NULL COMMENT '群组ID，雪花算法生成',
    `group_type`  TINYINT(1) DEFAULT 1 COMMENT '群类型：0普通群，1临时群，2其他:TODO',
    `group_name` VARCHAR(255) NOT NULL COMMENT '名称',
    `announcement` VARCHAR(255) NOT NULL COMMENT '公告',
    `avatar` VARCHAR(255) DEFAULT '' COMMENT '用户头像',
    `avatar_thumb` VARCHAR(255) DEFAULT '' COMMENT '用户头像缩略图',
    PRIMARY KEY (group_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组信息表';

DROP TABLE `anyim_group_member`;
CREATE TABLE `anyim_group_member`(
    `group_id` BIGINT NOT NULL COMMENT '群组ID，雪花算法生成',
    `member_account` VARCHAR(255) NOT NULL COMMENT '成员账号',
    `member_nick_name` VARCHAR(255) NOT NULL COMMENT '成员昵称',
    `member_avatar_thumb` VARCHAR(255) DEFAULT '' COMMENT '成员头像缩略图',
    `member_role` TINYINT(1) NOT NULL COMMENT '成员角色：0普通成员，1普通管理员，2超级管理员，3群主',
    PRIMARY KEY (group_id, member_account)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '群组成员表';

