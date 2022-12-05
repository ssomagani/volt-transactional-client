package com.voltdb.clienttxn;

import static com.voltdb.clienttxn.Utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public abstract class Rollbackable extends VoltCompoundProcedure {

	protected String insertUndoLogProc;		// insert_undo_table
	protected String undoProc;				// delete if insert, insert if delete, or update
	protected String getUndoValsProc;		// table_select_by_id
	protected String theProc;				// table_insert/table_update/table_delete...
	protected String txnId;

	protected VoltTable procArgs, getUndoValsProcArgs;

	// Flag to track cases where the stored proc doesn't result in any row changes
	protected Boolean undoPossible = Boolean.TRUE; 

	protected void finish(ClientResponse[] resp) {
		System.out.println("finish");
		if(resp[0].getStatus() == ClientResponse.SUCCESS)
			completeProcedure(resp[0].getResults());
		else 
			throwException(resp[0].getStatusString());
	}

	protected void callTheProc(ClientResponse[] resp) {
		System.out.println("callTheProc");
		verifyAndGetTheResults(resp);
		if(procArgs.advanceRow()) {
			System.out.println(theProc);
			Arrays.stream(procArgs.getRowObjects()).forEach((x) -> System.out.println(x));
			queueProcedureCall(theProc, procArgs.getRowObjects());
		} else {
			queueProcedureCall(theProc);
		}
	}

	protected void selectExistingVals(ClientResponse[] resp) {
		if(getUndoValsProcArgs.advanceRow())
			queueProcedureCall(getUndoValsProc, getUndoValsProcArgs.getRowObjects());
		else 
			queueProcedureCall(getUndoValsProc);
	}

	protected void insertUndoLog(ClientResponse[] resp) throws IOException {
		VoltTable result = verifyAndGetTheResults(resp);
		insertUndoLog(result);
	}
	
	protected void insertUndoLog(VoltTable undoValsTable) throws IOException {
		if(undoValsTable != null) {
			undoPossible = Boolean.TRUE;
			Object[] args = new Object[3];
			args[0] = txnId;
			args[1] = undoProc;
			args[2] = getByteArray(undoValsTable);
			queueProcedureCall(UNDO_LOG_INSERT, args);
		} else {
			undoPossible = Boolean.FALSE;
		}
	}

	protected byte[] getByteArray(VoltTable argsTable) throws IOException {
		Object[] argsArray = new Object[argsTable.getColumnCount()];
		for(int i=0; i<argsArray.length; i++) {
			argsArray[i] = argsTable.get(i);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(argsArray);
		return bos.toByteArray();
	}
}
