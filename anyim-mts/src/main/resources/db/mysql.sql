use `anyim`;
drop table `anyim_mts_object`;
CREATE TABLE `anyim_mts_object`
(
    `object_id` BIGINT NOT NULL COMMENT '富媒体对象唯一ID，采用雪花算法',
    `object_type` TINYINT(1) NOT NULL COMMENT '对象类型: 0图像，1音频，2视频，3文件',
    `foreign_id` VARCHAR(255) NOT NULL COMMENT '外键id',
    `created_account` VARCHAR(255) NOT NULL COMMENT '创建者',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY(`object_id`),
    INDEX `idx_foreign_id`(foreign_id)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '富媒体对象表，MTS服务的首表';

drop table `anyim_mts_file`;
CREATE TABLE `anyim_mts_file`
(
    `file_id` VARCHAR(255) NOT NULL COMMENT '文件唯一ID，采用文件md5计算方式',
    `file_type` VARCHAR(64) NOT NULL COMMENT '文件类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小',
    `file_url` VARCHAR(512) NOT NULL COMMENT '文件下载url地址',
    `expire` BIGINT NOT NULL COMMENT '过期时间',
    `created_account` VARCHAR(255) NOT NULL COMMENT '创建者',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY(`file_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '文件对象信息表';

drop table `anyim_mts_image`;
CREATE TABLE `anyim_mts_image`
(
    `image_id` VARCHAR(255) NOT NULL COMMENT '图像唯一ID，采用文件md5计算方式',
    `image_type` VARCHAR(64) NOT NULL COMMENT '图像类型',
    `image_size` BIGINT NOT NULL COMMENT '图像大小',
    `origin_url` VARCHAR(512) NOT NULL COMMENT '原始图下载url地址',
    `thumb_url` VARCHAR(512) DEFAULT NULL COMMENT '缩略图下载url地址',
    `expire` BIGINT NOT NULL COMMENT '过期时间',
    `created_account` VARCHAR(255) NOT NULL COMMENT '创建者',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY(`image_id`)
) ENGINE=INNODB CHARSET=utf8mb3 COMMENT '图像表';