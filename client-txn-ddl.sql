
create table client_txn (
	id varchar NOT NULL, 
	creation_time TIMESTAMP NOT NULL,
	op_table varchar,
	primary key (id, creation_time)
	);
PARTITION table client_txn on column id;

create compound procedure from class com.voltdb.clienttxn.RollbackableTxn;

create procedure GetTxnRecords partition on table client_txn column id as select * from client_txn where id = ? order by creation_time asc;

create procedure DeleteTxnRecords partition on table client_txn column id as delete from client_txn where id = ?;

create compound procedure from class com.voltdb.clienttxn.CommitTxn;