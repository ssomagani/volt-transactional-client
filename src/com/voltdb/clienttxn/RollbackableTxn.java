package com.voltdb.clienttxn;

import org.apache.log4j.Logger;
import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class RollbackableTxn extends VoltCompoundProcedure {

	private VoltTable[] results;
	private String txnId;
	private String storedProc, getUndoValsProc, insertUndoLogProc, undoStoredProc;
	private VoltTable getUndoValsProcArgs, procArgs;
	
	static Logger logger = Logger.getLogger("PROCEDURE");

	public VoltTable[] run(
			String txnId,
			String insertUndoLogProc, 
			String getUndoValsProc, 
			String undoStoredProc, 
			String storedProc, 
			VoltTable getUndoValsProcArgs,
			VoltTable procArgs) {

		this.txnId = txnId;
		this.insertUndoLogProc = insertUndoLogProc;
		this.getUndoValsProc = getUndoValsProc;
		this.undoStoredProc = undoStoredProc;
		this.storedProc = storedProc;
		this.getUndoValsProcArgs = getUndoValsProcArgs;
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
		if(getUndoValsProcArgs.advanceRow()) {
			System.out.println(getUndoValsProcArgs.getRowObjects()[0]);
			System.out.println(getUndoValsProc);
			queueProcedureCall(getUndoValsProc, getUndoValsProcArgs.getRowObjects());
		}
	}

	private void callInsertUndoLogProc(ClientResponse[] resp) {
		VoltTable result = verifyAndGetTheResults(resp);
		Object[] args = new Object[result.getColumnCount() + 2];
		args[0] = txnId;
		args[1] = undoStoredProc;
		for(int i=0; i<result.getColumnCount(); i++) {
			args[i+2] = result.get(i);
		}
		queueProcedureCall(insertUndoLogProc, args);
	}

	private void writeTxnRecord(ClientResponse[] resp) {
		VoltTable result = verifyAndGetTheResults(resp, 1);
		queueProcedureCall("client_txn.insert", 
				result.getString("txn_id"), 
				result.getTimestampAsTimestamp("creation_time"),
				result.getString("tbl")
				);
	}

	private void runStoredProc(ClientResponse[] resp) {
		verifyAndGetTheResults(resp);
		if(procArgs.advanceRow())
			queueProcedureCall(storedProc, procArgs.getRowObjects());
	}

	private void finish(ClientResponse[] resp) {
		completeProcedure(results);
	}

	private VoltTable verifyAndGetTheResults(ClientResponse[] resp) {
		return verifyAndGetTheResults(resp, 0);
	}
	
	private VoltTable verifyAndGetTheResults(ClientResponse[] resp, int resultIndex) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throw new CompoundProcAbortException(resp[0].getStatusString());
		} else {
			VoltTable results = resp[0].getResults()[resultIndex];
			if(results.advanceRow())
				return results;
		}
		return null;
	}
}
