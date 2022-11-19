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
	
	public TransactionalClient(Client2Config config) {
		client = ClientFactory.createClient(config);
	}
	
	public void connect(String serverString) throws IOException {
		client.connectSync(serverString);
	}

	public String startTransaction() throws IOException, ProcCallException {
		UUID uuid = UUID.randomUUID();
		txnId = uuid.toString();
		return txnId;
	}
	
	public ClientResponse select(String selectProc, VoltTable procArgs) throws IOException, ProcCallException {
		if(procArgs.advanceRow())
			return client.callProcedureSync(selectProc, procArgs.getRowObjects());
		return null;
	}
	
	public ClientResponse update(
			String txnId,
			String table,
			String updateProc,
			VoltTable whereClauseArgs,
			VoltTable procArgs
			) throws IOException, ProcCallException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = "insert_undo_" + table;
		allArgs[2] = table + "_select_by_id";
		allArgs[3] = updateProc;
		allArgs[4] = updateProc;
		allArgs[5] = whereClauseArgs;
		allArgs[6] = procArgs;
		ClientResponse resp = client.callProcedureSync("RollbackableTxn", allArgs);
		return resp;
	}
	
	public ClientResponse insert(
			String txnId,
			String table,
			VoltTable procArgs
			) throws IOException, ProcCallException {
		Object[] allArgs = new Object[5];
		allArgs[0] = txnId;
		allArgs[1] = table + "_undo_insert_blank";
		allArgs[2] = table + "_delete";
		allArgs[3] = table + "_insert";
		allArgs[4] = procArgs;
		return client.callProcedureSync("Insert", allArgs);
	}
	
	public ClientResponse delete(
		String txnId,
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
			String txnId, 
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
		ClientResponse resp = client.callProcedureSync("RollbackableTxn", allArgs);
		return resp;
	}
	
	public void rollback() throws IOException, ProcCallException {
		client.callProcedureSync("Rollback", txnId);
	}
	
	public void commit() throws IOException, ProcCallException {
		client.callProcedureSync("Commit", txnId);
	}
}
