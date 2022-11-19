---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure Procedure if exists;
drop procedure Insert if exists;
drop procedure InsertAfterFKCheck if exists;
drop procedure Update if exists;
drop procedure Delete if exists;
drop procedure Commit if exists;
drop procedure Rollback if exists;
drop procedure GetTxnRecords if exists;
drop procedure DeleteTxnRecords if exists;
drop procedure GetForeignKeys if exists;

DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table client_txn if exists;
drop table product_undo if exists;
drop table user_undo_usage if exists;
drop table foreign_keys if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table client_txn (
	id varchar NOT NULL, 
	creation_time TIMESTAMP NOT NULL,
	op_table varchar,
	primary key (id, creation_time)
	);
PARTITION table client_txn on column id;

create table foreign_keys (
	home_table varchar NOT NULL,
	home_column varchar NOT NULL,
	foreign_table varchar NOT NULL,
	foreign_column varchar NOT NULL,
	unique(home_table, home_column, foreign_table, foreign_column)
);
partition table foreign_keys on column home_table;

CREATE_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

create procedure GetTxnRecords 
	partition on table client_txn column id 
	as 
	select * from client_txn where id = ? order by creation_time asc;

create procedure DeleteTxnRecords 
	partition on table client_txn column id 
	as 
	delete from client_txn where id = ?;

create procedure GetForeignKeys 
	partition on table foreign_keys column home_table
	as 
	select * from foreign_keys where home_table = ?;

create compound procedure from class com.voltdb.clienttxn.Procedure;
create compound procedure from class com.voltdb.clienttxn.Insert;
create compound procedure from class com.voltdb.clienttxn.InsertAfterFKCheck;
create compound procedure from class com.voltdb.clienttxn.Update;
create compound procedure from class com.voltdb.clienttxn.Delete;
create compound procedure from class com.voltdb.clienttxn.Commit;
create compound procedure from class com.voltdb.clienttxn.Rollback;

CREATE_PROC_BATCH
---------------------------------------------------------------------