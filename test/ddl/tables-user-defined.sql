---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table user if exists;
drop table product if exists;
drop table user_usage if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table user (
	id bigint not null primary key,
	name varchar(16),
	last_seen TIMESTAMP DEFAULT NOW
);
partition table user on column id;

CREATE table product (
	id bigint not null primary key,
	name varchar(50) not null,
	unit_cost bigint not null
);
partition table product on column id;

create table user_usage (
	user_id bigint not null,
	product_id bigint not null,
	session_id bigint not null,
	usage bigint  not null,
	last_used TIMESTAMP DEFAULT NOW not null,
	primary key (user_id, product_id, session_id)
);
partition table user_usage on column user_id;

CREATE_TABLE_BATCH
---------------------------------------------------------------------