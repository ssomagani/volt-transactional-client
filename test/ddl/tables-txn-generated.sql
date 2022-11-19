---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table user_undo if exists;
drop table product_undo if exists;
drop table user_usage_undo if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table user_undo (
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id bigint not null,
	name varchar(16),
	last_seen TIMESTAMP DEFAULT NOW,
	primary key (txn_id, creation_time)
);
partition table user_undo on column txn_id;

CREATE table product_undo (
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id bigint not null,
	name varchar(50) not null,
	unit_cost bigint not null,
	primary key (txn_id, creation_time)
);
partition table product_undo on column txn_id;

create table user_usage_undo (
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	user_id bigint not null,
	product_id bigint not null,
	session_id bigint not null,
	usage bigint  not null,
	last_used TIMESTAMP DEFAULT NOW not null,
	primary key (txn_id, creation_time)
);
partition table user_usage_undo on column txn_id;

CREATE_TABLE_BATCH
---------------------------------------------------------------------