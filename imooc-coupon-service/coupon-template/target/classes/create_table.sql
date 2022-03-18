create table if NOT exists `coupon_template`(
    `id` int(11) auto_increment not null default null comment 'ID',
    `available` tinyint(1) not null default 0 comment '是否可用',
    `expired` tinyint(1) not null default 0 comment '是否过期',
    `name` varchar(64) not null  comment '优惠券名称',
    `logo` varchar(256) not null comment 'logo',
    `intro` varchar(256) not null comment '介绍',
    `category` varchar(64) not null comment '类型',
    `product_line` int(11) not null default 0 comment '产品线',
    `coupon_count` int(11) not null default 0 comment '优惠券数量',
    `create_time` datetime not null default '0000-01-01 00:00:00' comment '创建日期',
    `user_id` bigint(20) not null default 0 comment '创建人',
    `template_key` varchar(128) not null comment '模板ID',
    `target` int(11) not null default 0 comment '发放对象',
    `rule` varchar(1024) not null comment '优惠券规则',
    PRIMARY key (`id`),
    unique (`name`),
    index (`category`),
    index (`user_id`)
)engine=innoDB default charset=utf8;

create table if NOT exists `coupon`(
    `id` int(11) auto_increment not null default null comment 'ID',
    `template_id` int(11) not null default 0 comment '优惠券模板id',
    `coupon_code` varchar(64) not null comment '优惠券码',
    `user_id` bigint(20) not null default 0 comment '优惠券所有者',
    `assign_time` datetime not null default '0000-01-01 00:00:00' comment '领取日期',
    `status` int(11) not null default 0 comment '优惠券状态',
    PRIMARY key (`id`),
    index (`template_id`),
    index (`user_id`)
)engine=innoDB default charset=utf8;

CREATE  TABLE  table_name(
     属性名  数据类型[约束条件],
     ......
     属性名  数据类型[约束条件]
    [UNIQUE  |  FULLTEXT   |   SPATIAL  ]   INDEX  |  KEY
    [  别名 ]  (  属性名   [(  长度  )]   [  ASC  |  DESC  )
);
