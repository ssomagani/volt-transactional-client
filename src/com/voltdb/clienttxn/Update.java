package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.applyToAllResults;

import org.voltdb.VoltTable;

public class Update extends Rollbackable {

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

		newStageList(this::selectRowsToBeAffected)
		.then((t) -> applyToAllResults(t, this::insertUndoLog))
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}
}
