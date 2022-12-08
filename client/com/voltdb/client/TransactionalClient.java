package com.voltdb.client;

import java.io.IOException;
import java.util.UUID;

import org.voltdb.VoltTable;
import org.voltdb.client.Client2;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class TransactionalClient {

	public final Client2 client;
	private String txnId;
	public VoltSchema schema;

	public TransactionalClient(Client2Config config) {
		client = ClientFactory.createClient(config);
	}

	public void connect(String serverString) throws IOException, ProcCallException {
		client.connectSync(serverString);
		schema = new VoltSchema(client);
	}

	public String startTransaction() throws IOException, ProcCallException {
		UUID uuid = UUID.randomUUID();
		txnId = uuid.toString();
		return txnId;
	}

	public ClientResponse select(String selectProc, VoltTable procArgs) throws IOException, ProcCallException {
		if(procArgs.advanceRow())
			return client.callProcedureSync(selectProc, procArgs.getRowObjects());
		else
			return client.callProcedureSync(selectProc);
	}

	public ClientResponse update(
			String tableName,
			Object... insertArgs
			) throws IOException, ProcCallException {
		ClientVoltTable table = schema.getTable(tableName);
		if(table == null)
			throw new RuntimeException("Schema for table " + tableName + " not found on the client.");
		VoltTable updateArgTable = table.getUpdateArgsTable(insertArgs);
		VoltTable undoArgTable = table.getLoadedPrimaryKeyTable(updateArgTable);
		
		Object[] allArgs = new Object[5];
		allArgs[0] = txnId;
		allArgs[1] = tableName.toUpperCase() + ".select";
		allArgs[2] = tableName.toUpperCase() + ".update";
		allArgs[3] = undoArgTable;
		allArgs[4] = updateArgTable;
		
		return client.callProcedureSync("Update", allArgs);
	}

	public ClientResponse insert(
			String tableName,
			Object... insertArgs
			) throws IOException, ProcCallException, RuntimeException {

		ClientVoltTable table = schema.getTable(tableName);
		if(table == null)
			throw new RuntimeException("Schema for table " + tableName + " not found on the client.");
		VoltTable insertArgTable = table.getInsertArgsTable(insertArgs);
		VoltTable undoArgTable = table.getLoadedPrimaryKeyTable(insertArgTable);

		Object[] allArgs = new Object[5];
		allArgs[0] = txnId;
		allArgs[1] = tableName.toUpperCase() + ".delete";
		allArgs[2] = tableName.toUpperCase() + ".insert";
		allArgs[3] = undoArgTable;
		allArgs[4] = insertArgTable;

		return client.callProcedureSync("Insert", allArgs);
	}

	public ClientResponse delete(
			String tableName,
			Object... args
			) throws IOException, ProcCallException {

		ClientVoltTable table = schema.getTable(tableName);
		VoltTable argTable = table.getPrimaryKeyTable();
		argTable.addRow(args);

		Object[] allArgs = new Object[5];
		allArgs[0] = txnId;
		allArgs[1] = tableName.toUpperCase() + ".select";
		allArgs[2] = tableName.toUpperCase() + ".insert";
		allArgs[3] = tableName.toUpperCase() + ".delete";
		allArgs[4] = argTable;
		return client.callProcedureSync("Delete", allArgs);
	}

	public ClientResponse deleteWhere(
			String table,
			VoltTable whereClauseArgs,
			VoltTable procArgs) throws IOException, ProcCallException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = table + "_select_by_id";
		allArgs[2] = "insert_undo_" + table;
		allArgs[3] = "insert_" + table;
		allArgs[4] = "delete_" + table;
		allArgs[5] = whereClauseArgs;
		allArgs[6] = procArgs;
		return client.callProcedureSync("Delete", allArgs);
	}

	public ClientResponse callProcedureSync(
			String getUndoValsProc, 
			String insertUndoLogProc, 
			String undoStoredProc, 
			String storedProc, 
			VoltTable getUndoValsProcArgs,
			VoltTable procArgs) 
					throws ProcCallException, IOException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = insertUndoLogProc;
		allArgs[2] = getUndoValsProc;
		allArgs[3] = undoStoredProc;
		allArgs[4] = storedProc;
		allArgs[5] = getUndoValsProcArgs;
		allArgs[6] = procArgs;
		ClientResponse resp = client.callProcedureSync("Procedure", allArgs);
		return resp;
	}

	public void rollback() throws IOException, ProcCallException {
		client.callProcedureSync("Rollback", txnId);
	}

	public void commit() throws IOException, ProcCallException {
		client.callProcedureSync("undo_log_delete", txnId);
	}

	public void setTxnId(String txnId) { // For test only
		this.txnId = txnId;
	}
}
