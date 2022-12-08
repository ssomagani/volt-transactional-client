package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.applyToAllResults;

import org.voltdb.VoltTable;

public class Update extends Rollbackable {

	public long run(
			String txnId,
			String selectProc, 
			String updateProc, 
			VoltTable getUndoValsProcArgs,
			VoltTable procArgs) {

		this.txnId = txnId;
		this.getUndoValsProc = selectProc;
		this.undoProc = updateProc;
		this.theProc = updateProc;
		this.getUndoValsProcArgs = getUndoValsProcArgs;
		this.procArgs = procArgs;

		newStageList(
				this::selectRowsToBeAffected).then(
				(t) -> applyToAllResults(t, this::insertUndoLog)).then(
				this::callTheProc).then(
				this::finish)
		.build();
		return 0;
	}
}
