package com.voltdb.client;

import java.io.IOException;
import java.util.UUID;

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

	public String startTransaction(String... storedProcs) throws IOException, ProcCallException {
		UUID uuid = UUID.randomUUID();
		txnId = uuid.toString();
		return txnId;
	}
	
	public ClientResponse callProcedureSync(
			String txnId, 
			String insertUndoLogProc, 
			String getUndoValsProc, 
			String undoStoredProc, 
			String storedProc, 
			Object... args) 
			throws ProcCallException, IOException {
		Object[] allArgs = new Object[args.length + 5];
		allArgs[0] = txnId;
		allArgs[1] = insertUndoLogProc;
		allArgs[2] = getUndoValsProc;
		allArgs[3] = undoStoredProc;
		allArgs[4] = storedProc;
		ClientResponse resp = client.callProcedureSync("RollbackableTxn", args);
		return resp;
	}
	
	public void rollback() throws IOException, ProcCallException {
		client.callProcedureSync("RollbackTxn", null);
	}
	
	public void commit() {
		
	}
}
