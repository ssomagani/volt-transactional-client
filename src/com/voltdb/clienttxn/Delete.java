package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.applyToAllResults;

import org.voltdb.VoltTable;

public class Delete extends Rollbackable {

	public long run(
			String txnId,
			String selectProc, 
			String insertProc, 
			String deleteProc, 
			VoltTable whereClauseArgs) {

		this.txnId = txnId;
		this.getUndoValsProc = selectProc;
		this.undoProc = insertProc;
		this.theProc = deleteProc;
		this.procArgs = whereClauseArgs;
		this.getUndoValsProcArgs = whereClauseArgs;

		newStageList(this::selectRowsToBeAffected)
		.then((t) -> applyToAllResults(t, this::insertUndoLog))
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}
}
