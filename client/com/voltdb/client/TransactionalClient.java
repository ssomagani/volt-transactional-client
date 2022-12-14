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
	
	private Client2 client;
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
	
	public ClientResponse callSelect(String selectProc, VoltTable procArgs) throws IOException, ProcCallException {
		if(procArgs.advanceRow())
			return client.callProcedureSync(selectProc, procArgs.getRowObjects());
		return null;
	}
	
	public ClientResponse callUpdateProc(
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
	
	public ClientResponse callInsertProc() {
		//TODO Implement
		return null;
	}
	
	public ClientResponse callDeleteProc() {
		//TODO Implement
				return null;
	}
	
	public ClientResponse callProcedureSync(
			String txnId, 
			String insertUndoLogProc, 
			String getUndoValsProc, 
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
		client.callProcedureSync("RollbackTxn", txnId);
	}
	
	public void commit() throws IOException, ProcCallException {
		client.callProcedureSync("CommitTxn", txnId);
	}
}
