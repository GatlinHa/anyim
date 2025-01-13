use `anyim`;
drop table `anyim_mts_object`;
CREATE TABLE `anyim_mts_object`
(
    `obj_id` VARCHAR(255) NOT NULL COMMENT '文件对象唯一ID，采用文件md5计算方式',
    `obj_name` VARCHAR(255) NOT NULL COMMENT '对象名称',
    `obj_type` VARCHAR(32) NOT NULL COMMENT '对象类型',
    `obj_size` BIGINT NOT NULL COMMENT '对象大小',
    `ori_url` VARCHAR(512) NOT NULL COMMENT '原始下载url地址',
    `thumb_url` VARCHAR(512) DEFAULT NULL COMMENT '缩略（如果有）下载url地址',
    `expire` BIGINT NOT NULL COMMENT '过期时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY(`obj_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '文件对象信息表';