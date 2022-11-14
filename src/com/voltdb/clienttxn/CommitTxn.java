package com.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.client.ClientResponse;

public class CommitTxn extends VoltCompoundProcedure {

	public long run(String txnId) {
		newStageList(this::getTxnRecords)
		.then(this::runUndoProc)
		.then(this::finish);
		return 0;
	}
	
	private void getTxnRecords(ClientResponse[] resp) {
		
	}
	
	private void runUndoProc(ClientResponse[] resp) {
		
	}
	
	private void finish(ClientResponse[] resp) {
		
	}
}
