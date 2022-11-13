package com.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class RollbackableTxn extends VoltCompoundProcedure {

	private VoltTable[] results;
	private String txnId;
	private String storedProc, getUndoValsProc, insertUndoLogProc, undoStoredProc;
	private Object[] procArgs;

	public VoltTable[] run(
			String txnId,
			String insertUndoLogProc, 
			String getUndoValsProc, 
			String undoStoredProc, 
			String storedProc, 
			Object...procArgs) {

		this.txnId = txnId;
		this.insertUndoLogProc = insertUndoLogProc;
		this.getUndoValsProc = getUndoValsProc;
		this.undoStoredProc = undoStoredProc;
		this.storedProc = storedProc;
		this.procArgs = procArgs;

		newStageList(this::callGetUndoValsProc)
		.then(this::callInsertUndoLogProc)
		.then(this::writeTxnRecord)
		.then(this::runStoredProc)
		.then(this::finish)
		.build();
		return this.results;
	}

	private void callGetUndoValsProc(ClientResponse[] resp) {
		callProcedure(getUndoValsProc, procArgs);
	}

	private void callInsertUndoLogProc(ClientResponse[] resp) {
		VoltTable result = verifyAndGetTheResults(resp);
		if(result.advanceRow()) {
			Object[] args = new Object[result.getColumnCount() + 2];
			args[0] = txnId;
			args[1] = undoStoredProc;
			for(int i=0; i<result.getColumnCount(); i++) {
				args[i+2] = result.get(i);
			}
			callProcedure(insertUndoLogProc, args);
		}
	}

	private void writeTxnRecord(ClientResponse[] resp) {
		VoltTable result = verifyAndGetTheResults(resp);
		if(result.advanceRow()) {
			callProcedure("client_txn.insert", 
					result.getString("txn_id"), 
					result.getTimestampAsTimestamp("creation_time"),
					result.getString("tbl")
					);
		}
	}

	private void runStoredProc(ClientResponse[] resp) {
		verifyAndGetTheResults(resp);
		queueProcedureCall(storedProc, procArgs);
	}

	private void finish(ClientResponse[] resp) {
		completeProcedure(verifyAndGetTheResults(resp));
	}

	private VoltTable verifyAndGetTheResults(ClientResponse[] resp) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throw new CompoundProcAbortException(resp[0].getStatusString());
		} else {
			return resp[0].getResults()[0]; 
		}
	}
}
