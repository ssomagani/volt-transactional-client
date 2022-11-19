package com.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;
import static com.voltdb.clienttxn.Utils.*;

public class Commit extends VoltCompoundProcedure {
	
	private String txnId;

	public long run(String txnId) {
		this.txnId = txnId;
		
		newStageList(this::getTxnRecords)
		.then(this::runUndoProc)
		.then(this::deleteTxnRecords)
		.then(this::finish)
		.build();
		return 0;
	}
	
	private void getTxnRecords(ClientResponse[] resp) {
		queueProcedureCall("GetTxnRecords", txnId);
	}
	
	private void runUndoProc(ClientResponse[] resp) {
		applyToResults(resp, 0, this::deleteFromEachUndoRecordsTable);
	}
	
	private void deleteFromEachUndoRecordsTable(VoltTable undoRecordsTable) {
		String opTable = undoRecordsTable.getString(2);
		queueProcedureCall(opTable + "_delete", txnId);
	}
	
	private void deleteTxnRecords(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			queueProcedureCall("DeleteTxnRecords", txnId);
	}
	
	private void finish(ClientResponse[] resp) {
		if(allSuccessResponses(resp))
			completeProcedure(resp[0].getResults());
		else
			throwException(resp[0].getStatusString());
	}
}
