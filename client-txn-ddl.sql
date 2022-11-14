
create table client_txn (
	id varchar NOT NULL, 
	creation_time TIMESTAMP NOT NULL,
	op_table varchar,
	primary key (id, creation_time)
	);

create compound procedure from class com.voltdb.clienttxn.RollbackableTxn;

create procedure GetRecords partition on table client_txn column id as select * from client_txn where id = ?;
