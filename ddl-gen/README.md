# Generate DDL for creating the transaction tables and procedures

## Convention

* Tables
  * table_undo - Undo log of transactions in progress that are modifying this table (partitioned on txn_id)
  * client_txn - Records of transactions in progress (partitioned on txn_id)
  * foreign_keys - Table to hold the foreign keys

* Procedures
  * table_undo_insert         - insert into undo table
  * table_undo_insert_noargs_ - insert into undo table without any args
  * table_select_by_id        - 
  * table_insert
  * table_update
  * table_delete
