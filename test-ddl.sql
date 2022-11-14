
create table test (id integer not null, name varchar(12) not null, primary key (id));
partition table test on column id;

create procedure test_proc partition on table test column id parameter 1 as update test set name = ? where id = ?;

create procedure test_select partition on table test column id as select id, name from test where id = ?;

--
-- Undo Log
--
create table undo_test(
	txn_id varchar not null,
	creation_time timestamp not null, 
	undo_proc varchar not null, 
	id integer not null, 
	name varchar(12) not null
);
partition table undo_test on column txn_id;

create procedure insert_undo_test 
	as BEGIN
		insert into undo_test values ?, NOW, ?, ?, ?;
		select txn_id, creation_time, 'undo_test' as TBL from undo_test order by creation_time desc limit 1;
	END;
	

create procedure delete_undo_test as delete from undo_test where txn_id = ?;

create procedure get_undo_test as select * from undo_test where txn_id = ?;

