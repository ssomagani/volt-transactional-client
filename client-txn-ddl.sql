
create table client_txn (
	id varchar NOT NULL, 
	creation_time TIMESTAMP NOT NULL,
	op_table varchar,
	primary key (id, creation_time)
	);
