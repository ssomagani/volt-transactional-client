package com.voltdb.clienttxn;

import java.io.IOException;

import org.voltdb.VoltTable;

public class Delete extends Rollbackable {

	public long run(
			String txnId,
			String getUndoValsProc, 
			String undoProc, 
			String theProc, 
			VoltTable procArgs) {

		this.txnId = txnId;
		this.getUndoValsProc = getUndoValsProc;
		this.undoProc = undoProc;
		this.theProc = theProc;
		this.procArgs = procArgs;

		newStageList(this::selectExistingVals)
		.then(t -> {
			try {
				insertUndoLog(t);
			} catch (IOException e) {
				e.printStackTrace();
			}
		})
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}
}
