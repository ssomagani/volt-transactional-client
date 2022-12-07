package com.voltdb.clienttxn;

import java.io.IOException;

import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

import static com.voltdb.clienttxn.Utils.*;

public class Insert extends Rollbackable {
	
	private VoltTable undoArgs;

	public long run(
			String txnId,
			String deleteProc, 
			String insertProc, 
			VoltTable deleteProcArgs,
			VoltTable insertProcArgs) {

		this.txnId = txnId;
		this.undoProc = deleteProc;
		this.theProc = insertProc;
		this.undoArgs = deleteProcArgs;
		this.procArgs = insertProcArgs;

		newStageList(this::insertUndoLog)
		.then(this::callTheProc)
		.then(this::finish)
		.build();
		return 0;
	}

	protected void insertUndoLog(ClientResponse[] resp) {
		System.out.println("callInsertUndoLogProc");
		if(undoArgs.advanceRow()) {
			Object[] args = new Object[3];
			args[0] = txnId;
			args[1] = undoProc;
			try {
				args[2] = getByteArray(undoArgs);
			} catch (IOException e) {
				e.printStackTrace();
				abortProcedure(e.getMessage());
			}
			queueProcedureCall(UNDO_LOG_INSERT, args);
			undoArgs.resetRowPosition();
		}
	}
}
