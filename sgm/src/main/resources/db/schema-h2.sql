DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user
(
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(30) NULL DEFAULT NULL COMMENT '姓名',
    age INT NULL DEFAULT NULL COMMENT '年龄',
    email VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
    version INT NULL DEFAULT NULL COMMENT '版本',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS role;

CREATE TABLE role
(
    id            BIGINT NOT NULL COMMENT '主键ID',
    role_name     VARCHAR(30) NULL DEFAULT NULL COMMENT '角色名',
    role_describe VARCHAR(30) NULL DEFAULT NULL COMMENT '角色描述',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user2;

CREATE TABLE user2
(
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(30) NULL DEFAULT NULL COMMENT '姓名',
    age INT NULL DEFAULT NULL COMMENT '年龄',
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS article;
CREATE TABLE article
(
    `id`        BIGINT NOT NULL AUTO_INCREMENT,
    `title`     varchar(30) NULL DEFAULT NULL,
    `name`     varchar(30) NULL DEFAULT NULL,
    `author`    varchar(30) NULL DEFAULT NULL,
    `content`   varchar(30) NULL DEFAULT NULL,
    `state`     INT NULL DEFAULT 1 COMMENT '状态',
    PRIMARY KEY (`id`)
)