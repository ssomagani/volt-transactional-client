package org.voltdb.client;

import java.io.IOException;
import java.util.UUID;

public class TransactionalClient {
	
	private Client2 client;
	
	public TransactionalClient(Client2Config config) {
		client = ClientFactory.createClient(config);
		
	}
	
	public void connect(String serverString) throws IOException {
		client.connectSync(serverString);
	}

	public String startTransaction(String... storedProcs) throws IOException, ProcCallException {
		UUID uuid = UUID.randomUUID();
		client.callProcedureSync("client_txns.insert", uuid.toString(), 0, "");
		return uuid.toString();
	}
	
	public ClientResponse callProcedureSync(String txnId, int counter, String storedProc, Object... args) throws ProcCallException, IOException {
		client.callProcedureSync("client_txns.insert", txnId, counter, "");
		ClientResponse resp = client.callProcedureSync(storedProc, args);
		return resp;
		
	}
	
	public void rollback() {
		
	}
	
	public void commit() {
		
	}

}
