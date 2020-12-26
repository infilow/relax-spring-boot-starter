CREATE TABLE IF NOT EXISTS audit_records
(
    `id`     int(11)      NOT NULL AUTO_INCREMENT,
    `app`    varchar(255) NOT NULL COMMENT '应用程序名称',
    `org`    varchar(255) DEFAULT NULL COMMENT '用户所属机构',
    `role`   varchar(255) DEFAULT NULL COMMENT '用户所属角色',
    `user`   varchar(255) NOT NULL COMMENT '用户名称',
    `act`    varchar(255) NOT NULL COMMENT '用户行为',
    `tags`   varchar(255) DEFAULT NULL COMMENT '用户行为标记',
    `time`   timestamp    NOT NULL COMMENT '行为执行时间',
    `succed` boolean      NOT NULL COMMENT '行为执行是否成功',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  AUTO_INCREMENT = 0 COMMENT '用户行为审计记录表';

-- Note: Please create indexes depends on your query scenarios.