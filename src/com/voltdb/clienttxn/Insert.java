package com.voltdb.clienttxn;

import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;

public class Insert extends Rollbackable {

	public long run(
			String txnId,
			String insertUndoLogProc, 
			String undoProc, 
			String theProc, 
			VoltTable procArgs) {

		this.txnId = txnId;
		this.insertUndoLogProc = insertUndoLogProc;
		this.undoProc = undoProc;
		this.theProc = theProc;
		this.procArgs = procArgs;

		newStageList(this::callInsertUndoLogProc)
		.then(this::writeTxnRecord)
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}

	protected void callInsertUndoLogProc(ClientResponse[] resp) {
		System.out.println("callInsertUndoLogProc");
		if(procArgs.advanceRow()) {
			Object[] args = new Object[3];
			args[0] = txnId;
			args[1] = undoProc;
			args[2] = procArgs.get("id", VoltType.BIGINT);
			queueProcedureCall(insertUndoLogProc, args);
			procArgs.resetRowPosition();
		}
	}
}
