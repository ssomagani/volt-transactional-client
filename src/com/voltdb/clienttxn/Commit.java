package com.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.client.ClientResponse;
import static com.voltdb.clienttxn.Utils.*;

public class Commit extends VoltCompoundProcedure {
	
	private String txnId;

	public long run(String txnId) {
		this.txnId = txnId;
		
		newStageList
		(this::deleteUndoLogs)
		.then(this::finish)
		.build();
		return 0;
	}
	
	private void deleteUndoLogs(ClientResponse[] resp) {
		queueProcedureCall(UNDO_LOG_DELETE, txnId);
	}
	
	private void finish(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			completeProcedure(resp[0].getResults());
		else
			throwException(resp[0].getStatusString());
	}
}
