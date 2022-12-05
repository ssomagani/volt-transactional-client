package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class Rollback extends VoltCompoundProcedure {

	private String txnId;
	
	public long run(String txnId) {
		this.txnId = txnId;
		newStageList
		(this::getUndoLogs)
		.then(this::execUndoLogs)
		.then(this::deleteUndoLogs)
		.then(this::finish)
		.build();
		return 0;
	}
	
	private void getUndoLogs(ClientResponse[] resp) {
		queueProcedureCall(UNDO_LOG_SELECT, txnId);
	}
	
	private void execUndoLogs(ClientResponse[] resp) {
		applyToResults(resp, 0, t -> {
			try {
				runUndoProcs(t);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
	}
	
	private void runUndoProcs(VoltTable undoLogs) throws IOException, ClassNotFoundException {
		String undoProc = undoLogs.getString("undo_proc");
		byte[] undoArgsByteArray = undoLogs.getVarbinary("undo_args");
		
		ByteArrayInputStream bis = new ByteArrayInputStream(undoArgsByteArray);
		ObjectInput in = new ObjectInputStream(bis);
		Object[] deserArgs = (Object[]) in.readObject();
		
		queueProcedureCall(undoProc, deserArgs);
	}
	
	private void deleteUndoLogs(ClientResponse[] resp) {
		queueProcedureCall("undo_log_delete", txnId);
	}
	
	private void finish(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			completeProcedure(resp[0].getResults());
		else
			abortProcedure(resp[0].getStatusString());	
	}
}
