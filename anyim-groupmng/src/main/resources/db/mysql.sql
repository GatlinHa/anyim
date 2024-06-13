use `anyim`;
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
