create table tag
(
    id          bigint auto_increment comment 'id'
        primary key,
    tag_name    varchar(256)                       null comment '标签',
    user_id     bigint                             null comment '用户id',
    parent_id   bigint                             null comment '父标签_id',
    is_parent   tinyint                            null comment '0 -不是 1 -是父标签',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0                 not null comment '是否删除',
    constraint tag_name_uindex
        unique (tag_name)
)
    comment '标签';
