package com.voltdb.clienttxn;

import java.io.IOException;

import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

import static com.voltdb.clienttxn.Utils.*;

public class Insert extends Rollbackable {
	
	private VoltTable undoArgs;

	public long run(
			String txnId,
			String undoProc, 
			String theProc, 
			VoltTable undoArgs,
			VoltTable procArgs) {

		this.txnId = txnId;
		this.undoProc = undoProc;
		this.theProc = theProc;
		this.undoArgs = undoArgs;
		this.procArgs = procArgs;

		newStageList(t -> {
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

	protected void insertUndoLog(ClientResponse[] resp) throws IOException {
		System.out.println("callInsertUndoLogProc");
		if(undoArgs.advanceRow()) {
			Object[] args = new Object[3];
			args[0] = txnId;
			args[1] = undoProc;
			args[2] = getByteArray(undoArgs);
			
			queueProcedureCall(UNDO_LOG_INSERT, args);
			undoArgs.resetRowPosition();
		}
	}
}
