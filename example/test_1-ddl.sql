---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure delete_undo_test_2 if exists;
drop procedure get_undo_test_2 if exists;
drop procedure insert_undo_test_2 if exists;
drop procedure test_2_select if exists;
drop procedure test_2_proc if exists;

DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch DROP_TABLE_BATCH

drop table undo_test_2 if exists;
drop table test_2 if exists;

DROP_TABLE_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_TABLE_BATCH

create table test_2 (id integer not null, name varchar(12) not null, primary key (id));
partition table test_2 on column id;

create table undo_test_2(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test_2 on column txn_id;

CREATE_TABLE_BATCH
---------------------------------------------------------------------
load classes classes.jar;
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

create procedure test_1_select_by_id partition on table test column id as select id, name from test where id = ?;

create procedure test_2_proc 
	partition on table test_2 column id parameter 1 
	as 
	update test_2 set name = ? where id = ?;

create procedure insert_undo_test_2 partition on table undo_test_2 column txn_id parameter 0  
	as BEGIN
		insert into undo_test_2 values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test_2' as TBL from undo_test_2 order by creation_time desc limit 1;
	END;
	
create procedure delete_undo_test_2 partition on table undo_test_2 column txn_id as delete from undo_test_2 where txn_id = ?;

create procedure get_undo_test_2 
	partition on table undo_test_2 column txn_id 
	as 
	select undo_proc, name, id from undo_test_2 where txn_id = ? order by creation_time asc;

CREATE_PROC_BATCH