---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure user_undo_insert if exists;
drop procedure user_undo_delete if exists;
drop procedure user_undo_select_by_id if exists;

drop procedure product_undo_insert if exists;
drop procedure product_undo_delete if exists;
drop procedure product_undo_select_by_id if exists;

drop procedure user_usage_undo_insert if exists;
drop procedure user_usage_undo_delete if exists;
drop procedure user_usage_undo_select_by_id if exists;


DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

create procedure user_undo_delete 
	partition on table user_undo column txn_id
	as
	delete from user_undo where txn_id = ?;
	
create procedure user_undo_insert 
	partition on table user_undo column txn_id parameter 0  
	as BEGIN
		insert into user_undo values ?, NOW, ?, ?, ?, ?;
		select txn_id, creation_time, 'user_undo' as TBL from user_undo order by creation_time desc limit 1;
	END;
	
create procedure user_undo_insert_blank 
	partition on table user_undo column txn_id 
	as BEGIN 
		insert into user_undo (txn_id, creation_time, undo_proc, id) values (?, NOW, ?, ?);
		select txn_id, creation_time, 'user_undo' as TBL from user_undo order by creation_time desc limit 1;
	END;

create procedure user_undo_select_by_id 
	partition on table user_undo column txn_id
	as 
	select * from user_undo where txn_id = ? order by creation_time asc;
	
CREATE_PROC_BATCH
---------------------------------------------------------------------