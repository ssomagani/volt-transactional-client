package com.voltdb.clienttxn;

import org.voltdb.VoltCompoundProcedure;
import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class InsertAfterFKCheck extends VoltCompoundProcedure {

	private VoltTable tableAndRowToInsert;
	private String tableName;
	
	public long run(VoltTable args) {
		if(args.advanceRow()) {
			tableAndRowToInsert = args;
			newStageList(this::getFkeys)
			.then(this::checkAllFkeys)
			.then(this::insertIfPassed)
			.then(this::finish)
			.build();
		}
		return 0;
	}
	
	private void getFkeys(ClientResponse[] resp) {
		tableName = tableAndRowToInsert.getString(0).toLowerCase();
		queueProcedureCall("GetForeignKeys", tableName);
	}
	
	private void checkAllFkeys(ClientResponse[] resp) {
		VoltTable results = resp[0].getResults()[0];
		
		while(results.advanceRow()) {
			String home_column = results.getString(1).toLowerCase();
			int home_column_index = tableAndRowToInsert.getColumnIndex(home_column);
			Object homeColVal = tableAndRowToInsert.get(home_column_index);
			String foreign_table = results.getString(2).toLowerCase();
			String foreign_column = results.getString(3).toLowerCase();
			
			queueProcedureCall(foreign_table + "_" + foreign_column + "_exists", homeColVal);
		}
	}
	
	private void insertIfPassed(ClientResponse[] responses) {
		for(ClientResponse resp : responses) {
			if(resp.getResults()[0].advanceRow())
			if(resp.getResults()[0].getLong(0) == 0)
				abortProcedure("Attempted Foreign Key Constraint violation");
		}
		Object[] rowValues = new Object[tableAndRowToInsert.getColumnCount() - 1];
		for(int i=0; i<rowValues.length; i++) {
			rowValues[i] = tableAndRowToInsert.get(i+1);
		}
		queueProcedureCall(tableName + "_insert", rowValues);
	}
	
	private void finish(ClientResponse[] resp) {
		if(resp[0].getStatus() == ClientResponse.SUCCESS) {
			completeProcedure(resp[0].getResults());
		} else {
			abortProcedure(resp[0].getStatusString());
		}
	}
}
