package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.*;

import java.util.Arrays;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;

public class Rollback extends VoltCompoundProcedure {

	private String txnId;
	
	public long run(String txnId) {
		this.txnId = txnId;
		newStageList(this::getTxnRecords)
		.then(this::getUndoLogsByTable)
		.then(this::undoByTable)
		.then(this::getTxnRecords)
		.then(this::deleteFromEachUndoRecordsTable)
		.then(this::deleteTxnRecords)
		.then(this::finish)
		.build();
		return 0;
	}
	
	private void getTxnRecords(ClientResponse[] resp) {
		queueProcedureCall("GetTxnRecords", txnId);
	}
	
	private void getUndoLogsByTable(ClientResponse[] resp) {
		applyToResults(resp, 0, (x) -> queueProcedureCall(x.getString("op_table") + "_select_by_id", txnId));
	}
	
	private void undoByTable(ClientResponse[] resp) {
		applyToAllResponses(resp, this::runUndoProcs);
	}
	
	private void runUndoProcs(VoltTable undoLogs) {
		String undoProc = undoLogs.getString("undo_proc");
		if(undoProc.endsWith("_delete")) {
			queueProcedureCall(undoLogs.getString("undo_proc"), undoLogs.get("id", VoltType.INTEGER));
		} else {
			queueProcedureCall(undoLogs.getString("undo_proc"), Arrays.copyOfRange(undoLogs.getRowObjects(), 1, undoLogs.getColumnCount()));
		}
	}
	
	private void deleteFromEachUndoRecordsTable(ClientResponse[] resp) {
		applyToResults(resp, 0, (x) -> queueProcedureCall(x.getString("op_table") + "_delete", txnId));
	}
	
	private void deleteTxnRecords(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			queueProcedureCall("DeleteTxnRecords", txnId);
	}
	
	private void finish(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			completeProcedure(resp[0].getResults());
		else
			abortProcedure(resp[0].getStatusString());	
	}
}
