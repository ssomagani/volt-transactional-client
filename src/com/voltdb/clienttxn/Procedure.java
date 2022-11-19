package com.voltdb.clienttxn;

import org.apache.log4j.Logger;
import org.voltdb.VoltTable;

public class Procedure extends Rollbackable {

	static Logger logger = Logger.getLogger("PROCEDURE");

	public long run(
			String txnId,
			String getUndoValsProc, 
			String insertUndoLogProc, 
			String undoProc, 
			String theProc, 
			VoltTable getUndoValsProcArgs,
			VoltTable procArgs) {

		this.txnId = txnId;
		this.getUndoValsProc = getUndoValsProc;
		this.insertUndoLogProc = insertUndoLogProc;
		this.undoProc = undoProc;
		this.getUndoValsProcArgs = getUndoValsProcArgs;
		this.theProc = theProc;
		this.procArgs = procArgs;

		newStageList(this::callGetUndoValsProc)
		.then(this::callInsertUndoLogProc)
		.then(this::writeTxnRecord)
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}
}
