
create table client_txns (
	id varchar NOT NULL, 
	index INTEGER NOT NULL, 
	payload VARBINARY,
	primary key (id, index)
	);
 
create procedure StartClientTxn as insert into client_txns values (?, ?, ?);