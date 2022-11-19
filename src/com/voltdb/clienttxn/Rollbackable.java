package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.*;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public abstract class Rollbackable extends VoltCompoundProcedure {

	protected String insertUndoLogProc;		// insert_undo_table
	protected String undoProc;				// delete if insert, insert if delete, or update
	protected String getUndoValsProc;		// table_select_by_id
	protected String theProc;				// table_insert/table_update/table_delete...
	protected String txnId;

	protected VoltTable procArgs, getUndoValsProcArgs;

	// Flag to track cases where the stored proc doesn't result in any row changes
	protected Boolean undoPossible = Boolean.TRUE; 

	protected void writeTxnRecord(ClientResponse[] resp) {
		System.out.println("writeTxnRecord");
		VoltTable result = verifyAndGetTheResults(resp, 1);
		queueProcedureCall("client_txn.insert", 
				result.getString("txn_id"), 
				result.getTimestampAsTimestamp("creation_time"),
				result.getString("tbl")
				);
	}

	protected void finish(ClientResponse[] resp) {
		System.out.println("finish");
		if(resp[0].getStatus() == ClientResponse.SUCCESS)
			completeProcedure(resp[0].getResults());
		else 
			throwException(resp[0].getStatusString());
	}

	protected void callTheProc(ClientResponse[] resp) {
		System.out.println("callTheProc");
		verifyAndGetTheResults(resp);
		if(procArgs.advanceRow()) {
			queueProcedureCall(theProc, procArgs.getRowObjects());
		} else {
			
		}
	}

	protected void callGetUndoValsProc(ClientResponse[] resp) {
		if(getUndoValsProcArgs.advanceRow())
			queueProcedureCall(getUndoValsProc, getUndoValsProcArgs.getRowObjects());
	}

	protected void callInsertUndoLogProc(ClientResponse[] resp) {
		VoltTable result = verifyAndGetTheResults(resp);
		if(result != null) {
			Object[] args = new Object[result.getColumnCount() + 2];
			args[0] = txnId;
			args[1] = undoProc;
			for(int i=0; i<result.getColumnCount(); i++) {
				args[i+2] = result.get(i);
			}
			queueProcedureCall(insertUndoLogProc, args);
		} else {
			undoPossible = Boolean.FALSE;
		}
	}
}
