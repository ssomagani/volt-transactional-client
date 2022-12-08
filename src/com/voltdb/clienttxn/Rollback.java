package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;
import org.voltdb.types.TimestampType;

public class Rollback extends VoltCompoundProcedure {

	public long run(String txnId) {
		newStageList
		(
				(t) -> queueProcedureCall(UNDO_LOG_SELECT, txnId)).then(
				(t) -> applyToResults(t, this::runUndoProcs)).then(
				(t) -> queueProcedureCall(UNDO_LOG_DELETE, txnId)).then(
				this::finish)
		.build();
		return 0;
	}
	
	private void runUndoProcs(VoltTable undoLogs) {
		String undoProc = undoLogs.getString("undo_proc");
		byte[] undoArgsByteArray = undoLogs.getVarbinary("undo_args");
		
		ByteArrayInputStream bis = new ByteArrayInputStream(undoArgsByteArray);
		ObjectInput in;
		try {
			in = new ObjectInputStream(bis);
			Object[] deserArgs = (Object[]) in.readObject();
			for(int i=0; i<deserArgs.length; i++) {
				if(deserArgs[i] instanceof java.util.Date) {
					deserArgs[i] = new TimestampType((java.util.Date)deserArgs[i]);
				}
			}
			queueProcedureCall(undoProc, deserArgs);
		} catch (IOException e) {
			e.printStackTrace();
			abortProcedure(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			abortProcedure(e.getMessage());
		}
	}
	
	private void finish(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			completeProcedure(resp[0].getResults());
		else
			abortProcedure(resp[0].getStatusString());	
	}
}
