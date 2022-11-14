create table foreign_keys (
	home_table varchar,
	home_column varchar,
	foreign_table varchar,
	foreign_column varchar,
	unique(home_table, home_column, foreign_table, foreign_column)
);

create procedure GetForeignKeys as select * from foreign_keys where home_table = ?;
create compound procedure from class org.voltdb.clienttxn.InsertAfterFKCheck;

