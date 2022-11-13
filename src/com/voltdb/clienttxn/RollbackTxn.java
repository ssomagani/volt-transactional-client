package com.voltdb.clienttxn;

import java.util.Arrays;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class RollbackTxn extends VoltCompoundProcedure {

	private String txnId;
	private String[] getUndoLogsProcs, commitProcs;
	
	public VoltTable run(String txnId, String[] getUndoLogsProcs, String[] commitProcs) {
		this.txnId = txnId;
		this.getUndoLogsProcs = getUndoLogsProcs;
		this.commitProcs = commitProcs;
		newStageList(this::getUndoLogs)
		.then(this::runUndoProcs)
		.then(this::commit)
		.then(this::finish)
		.build();
		return null;
	}
	
	private void getUndoLogs(ClientResponse[] resp) {
		for(String getUndoLogsProc : getUndoLogsProcs) {
			queueProcedureCall(getUndoLogsProc, txnId);
		}
	}
	
	private void runUndoProcs(ClientResponse[] responses) {
		for(ClientResponse resp : responses) {
			if(resp.getStatus() == ClientResponse.SUCCESS) {
				VoltTable undoLogs = resp.getResults()[0];
				while(undoLogs.advanceRow()) {
					String undoProc = undoLogs.getString(1);
					Object[] args = Arrays.copyOfRange(undoLogs.getRowObjects(), 2, undoLogs.getColumnCount());
					queueProcedureCall(undoProc, args);
				}
			}
		}
	}
	
	private void commit(ClientResponse[] responses) {
		for(int i=0; i<responses.length; i++) {
			ClientResponse resp = responses[i];
			if(resp.getStatus() == ClientResponse.SUCCESS) {
				queueProcedureCall(commitProcs[i], txnId);
			} else {
				abortProcedure(resp.getStatusString());
			}
		}
	}
	
	private void finish(ClientResponse[] resp) {
		completeProcedure(resp[0].getResults());
	}
}
