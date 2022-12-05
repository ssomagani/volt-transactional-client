---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure undo_log_insert if exists;
drop procedure undo_log_delete if exists;
drop procedure undo_log_select if exists;
drop procedure Procedure if exists;
drop procedure Insert if exists;
drop procedure InsertAfterFKCheck if exists;
drop procedure Update if exists;
drop procedure Delete if exists;
drop procedure Commit if exists;
drop procedure Rollback if exists;
drop procedure GetForeignKeys if exists;

DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table undo_log if exists;
drop table foreign_keys if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table foreign_keys (
	home_table varchar NOT NULL,
	home_column varchar NOT NULL,
	foreign_table varchar NOT NULL,
	foreign_column varchar NOT NULL,
	unique(home_table, home_column, foreign_table, foreign_column)
);
partition table foreign_keys on column home_table;

create table undo_log (
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	undo_args varbinary(10000) not null,
	primary key (txn_id, creation_time)
);
partition table undo_log on column txn_id;

CREATE_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

create procedure GetForeignKeys 
	partition on table foreign_keys column home_table
	as 
	select * from foreign_keys where home_table = ?;

create procedure undo_log_insert 
	partition on table undo_log column txn_id 
	as 
	insert into undo_log values (?, NOW, ?, ?);
	
create procedure undo_log_delete 
	partition on table undo_log column txn_id 
	as 
	delete from undo_log where txn_id = ?;
	
create procedure undo_log_select 
	partition on table undo_log column txn_id 
	as 
	select * from undo_log where txn_id = ?;

create compound procedure from class com.voltdb.clienttxn.Procedure;
create compound procedure from class com.voltdb.clienttxn.Insert;
create compound procedure from class com.voltdb.clienttxn.InsertAfterFKCheck;
create compound procedure from class com.voltdb.clienttxn.Update;
create compound procedure from class com.voltdb.clienttxn.Delete;
create compound procedure from class com.voltdb.clienttxn.Commit;
create compound procedure from class com.voltdb.clienttxn.Rollback;

CREATE_PROC_BATCH
---------------------------------------------------------------------