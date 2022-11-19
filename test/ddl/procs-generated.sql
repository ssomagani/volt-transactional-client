---------------------------------------------------------------------
file -inlinebatch DROP_PROC_BATCH

drop procedure user_insert if exists;
drop procedure user_delete if exists;
drop procedure user_select_by_id if exists;

drop procedure product_insert if exists;
drop procedure product_delete if exists;
drop procedure product_select_by_id if exists;

drop procedure user_usage_insert if exists;
drop procedure user_usage_delete if exists;
drop procedure user_usage_select_by_id if exists;


DROP_PROC_BATCH
---------------------------------------------------------------------
file -inlinebatch CREATE_PROC_BATCH

-- User

create procedure user_insert 
	partition on table user column id 
	as 
	insert into user (id, name) values (?, ?);
	
create procedure user_delete 
	partition on table user column id 
	as 
	delete from user where id = ?;
	
create procedure user_select_by_id 
	partition on table user column id 
	as
	select * from user where id = ?;

-- Product

create procedure product_insert 
	partition on table product column id 
	as 
	insert into product (id, name, unit_cost) values (?, ?, ?);
	
create procedure product_delete 
	partition on table product column id 
	as 
	delete from product where id = ?;
	
create procedure product_select_by_id 
	partition on table product column id 
	as
	select * from product where id = ?;
	
-- User_Usage

create procedure user_usage_insert 
	partition on table user_usage column user_id 
	as 
	insert into user_usage (user_id, product_id, session_id, usage) values (?, ?, ?, ?);
	
create procedure user_usage_delete 
	partition on table user_usage column user_id 
	as 
	delete from user_usage where user_id = ?;
	
create procedure user_usage_select_by_id 
	partition on table user_usage column user_id
	as
	select * from user_usage where user_id = ?;

CREATE_PROC_BATCH
---------------------------------------------------------------------