# volt-transactional-client
Volt Transaction Client provides the framework to implement transactional semantics that are found in other RDBMSs

## Features
* Foreign Key Enforcement
* Client-side Transactions

## How to use

### Foreign Key Enforcement
* Initial Setup - Run `sqlcmd < foreign-keys-ddl.sql` to create the schema and procedures for foreign key enforcement.
* Load foreign keys - `insert into foreign_keys values ('auth_info', 'sim_id', 'sim', 'id');` to declare that the column sim_id in the table auth_info refers to the column id in the table sim.
* Integrate - Instead of calling your insert procedure directly, call the `InsertAfterFKCheck` procedure by providing the name of the table to insert into as the first argument, and the values to insert as the following arguments.
* Example - `params.addRow("AuthenticationInformation", 3, 3, 3, 3, 1);
		ClientResponse resp = client.callProcedure("InsertAfterFKCheck", params);`

### Client-side Transactions
* Initial Setup - Run `sqlcmd < client-txn-ddl.sql` to create the schema and procedures for client-side transaction control.
* Build the library - `javac -cp /.../voltdbclient-12.0.jar:/.../voltdb-12.0.jar:/.../log4j-1.x.jar -d bin $(find . -name "*.java"); jar -cvf classes.jar -C bin .`
* Integrate - Add classes.jar to your application's classpath. Instead of using Volt's org.voltdb.client.Client class, use com.voltdb.client.TransactionalClient.
* Example -
  * Start Transaction - `String txnId = client.startTransaction()`
  * Call Procedure - `response = client.callProcedureSync(txnId, "proc"...)`
  * Call Procedure - `response = client.callProcedureSync(txnId, "proc"...)`
  * ***
  * Call Select Procedure - `response = client.callSelect("proc")`
  * Choose to commit or rollback - `client.commit()` or `client.rollback`
* Refer to example in client/com/voltdb/example
