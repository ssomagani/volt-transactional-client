file -inlinebatch DROP_PROC_BATCH

drop procedure delete_undo_test if exists;
drop procedure get_undo_test if exists;
drop procedure insert_undo_test if exists;
drop procedure test_select if exists;
drop procedure test_proc if exists;

drop procedure delete_undo_test_1 if exists;
drop procedure get_undo_test_1 if exists;
drop procedure insert_undo_test_1 if exists;
drop procedure test_1_select if exists;
drop procedure test_1_proc if exists;

drop procedure delete_undo_test_2 if exists;
drop procedure get_undo_test_2 if exists;
drop procedure insert_undo_test_2 if exists;
drop procedure test_2_select if exists;
drop procedure test_2_proc if exists;

DROP_PROC_BATCH

file -inlinebatch DROP_TABLE_BATCH

drop table undo_test if exists;
drop table test if exists;
drop table undo_test_1 if exists;
drop table test_1 if exists;
drop table undo_test_2 if exists;
drop table test_2 if exists;

DROP_TABLE_BATCH

file -inlinebatch CREATE_TABLE_BATCH

create table test (id integer not null, name varchar(12) not null, primary key (id));
partition table test on column id;
create table test_1 (id integer not null, name varchar(12) not null, primary key (id));
partition table test_1 on column id;
create table test_2 (id integer not null, name varchar(12) not null, primary key (id));
partition table test_2 on column id;

--
-- Undo Log Tables
--
create table undo_test(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test on column txn_id;

create table undo_test_1(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test_1 on column txn_id;

create table undo_test_2(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test_2 on column txn_id;

CREATE_TABLE_BATCH

load classes classes.jar;

file -inlinebatch CREATE_PROC_BATCH

create procedure test_proc partition on table test column id parameter 1 as update test set name = ? where id = ?;
create procedure test_1_proc partition on table test_1 column id parameter 1 as update test_1 set name = ? where id = ?;
create procedure test_2_proc partition on table test_2 column id parameter 1 as update test_2 set name = ? where id = ?;

create procedure test_select partition on table test column id as select id, name from test where id = ?;
create procedure test_1_select partition on table test_1 column id as select id, name from test_1 where id = ?;
create procedure test_2_select partition on table test_2 column id as select id, name from test_2 where id = ?;


--
-- Undo Log Procedures
--
create procedure insert_undo_test partition on table undo_test column txn_id parameter 0  
	as BEGIN
		insert into undo_test values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test' as TBL from undo_test order by creation_time desc limit 1;
	END;

create procedure insert_undo_test_1 partition on table undo_test_1 column txn_id parameter 0  
	as BEGIN
		insert into undo_test_1 values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test_1' as TBL from undo_test_1 order by creation_time desc limit 1;
	END;
	
create procedure insert_undo_test_2 partition on table undo_test_2 column txn_id parameter 0  
	as BEGIN
		insert into undo_test_2 values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test_2' as TBL from undo_test_2 order by creation_time desc limit 1;
	END;
	

create procedure delete_undo_test partition on table undo_test column txn_id as delete from undo_test where txn_id = ?;
create procedure delete_undo_test_1 partition on table undo_test_1 column txn_id as delete from undo_test_1 where txn_id = ?;
create procedure delete_undo_test_2 partition on table undo_test_2 column txn_id as delete from undo_test_2 where txn_id = ?;

create procedure get_undo_test partition on table undo_test column txn_id as select undo_proc, name, id from undo_test where txn_id = ? order by creation_time asc;
create procedure get_undo_test_1 partition on table undo_test_1 column txn_id as select undo_proc, name, id from undo_test_1 where txn_id = ? order by creation_time asc;
create procedure get_undo_test_2 partition on table undo_test_2 column txn_id as select undo_proc, name, id from undo_test_2 where txn_id = ? order by creation_time asc;

CREATE_PROC_BATCH