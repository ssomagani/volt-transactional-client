package org.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class RollbackableTransaction extends VoltCompoundProcedure {

//	private static final SQLStmt INSERT_TXN_META = new SQLStmt("insert into CLIENT_TXN values (?, ?, ?)");
	
	private VoltTable[] results;
	private String storedProc;
	private String saveStateStoredProc;
	private Object[] params;
	
	public VoltTable[] run(String saveStateStoredProc, String storedProc, Object...params) {
		
		this.saveStateStoredProc = saveStateStoredProc;
		this.storedProc = storedProc;
		this.params = params;
		
		newStageList(this::saveStateForRollback)
		.then(this::runStoredProc)
		.then(this::finish)
		.build();
		return this.results;
	}
	
	private void saveStateForRollback(ClientResponse[] res) {
		queueProcedureCall(saveStateStoredProc, params);
	}
	
	private void runStoredProc(ClientResponse[] res) {
		queueProcedureCall(storedProc, params);
	}
	
	private void finish(ClientResponse[] res) {
		if(res.length > 0) {
			results = res[0].getResults();
		}
	}
}
