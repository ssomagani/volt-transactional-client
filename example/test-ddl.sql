---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure delete_undo_test if exists;
drop procedure get_undo_test if exists;
drop procedure insert_undo_test if exists;
drop procedure test_select if exists;
drop procedure test_proc if exists;

DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table undo_test if exists;
drop table test if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table test (id integer not null, name varchar(12) not null, primary key (id));
partition table test on column id;

create table undo_test(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test on column txn_id;

CREATE_TABLE_BATCH
---------------------------------------------------------------------
load classes classes.jar;
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

create procedure test_select_by_id partition on table test column id as select id, name from test where id = ?;
create procedure test_restore_row partition on table test column id as insert into test values (?, ?);

create procedure test_proc 
	partition on table test column id parameter 1 
	as 
	update test set name = ? where id = ?;

create procedure insert_undo_test partition on table undo_test column txn_id parameter 0  
	as BEGIN
		insert into undo_test values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test' as TBL from undo_test order by creation_time desc limit 1;
	END;

create procedure insert_noargs_undo_test partition on table undo_test column txn_id parameter 0  
	as BEGIN
		insert into undo_test values ?, NOW, ?, -1, "";
		select txn_id, creation_time, 'undo_test' as TBL from undo_test order by creation_time desc limit 1;
	END;
	
create procedure delete_undo_test partition on table undo_test column txn_id as delete from undo_test where txn_id = ?;

create procedure get_undo_test 
	partition on table undo_test column txn_id 
	as 
	select undo_proc, name, id from undo_test where txn_id = ? order by creation_time asc;

CREATE_PROC_BATCH