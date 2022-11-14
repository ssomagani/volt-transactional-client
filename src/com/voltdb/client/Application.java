package com.voltdb.client;

import java.io.IOException;

import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class Application {

	public static void main(String[] args) {
		ClientResponse response = null;
		
		Client2Config config = new Client2Config();
		
		TransactionalClient client = new TransactionalClient(config);
		try {
			client.connect("localhost");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// ----- START TRANSACTION BOUNDARY -----------------------------------
		try {
			String txnId = client.startTransaction();
			
			VoltTable getUndoValsProcArgs = new VoltTable(
					new VoltTable.ColumnInfo("id", VoltType.INTEGER));
			Object[] vals = {1};
			getUndoValsProcArgs.addRow(vals);
			
			VoltTable procArgs = new VoltTable(
					new VoltTable.ColumnInfo("name", VoltType.STRING),
					new VoltTable.ColumnInfo("id", VoltType.INTEGER)
					);
			Object[] values = {"one", 1};
			procArgs.addRow(values);
			
			response = client.callProcedureSync(txnId, "insert_undo_test", "test_select", 
					"test_proc", "test_proc", getUndoValsProcArgs, procArgs);
			
			if(response.getStatus() == ClientResponse.SUCCESS) {
				client.commit();
			} else {
				client.rollback();
			}
			
		} catch (IOException | ProcCallException e1) {
			e1.printStackTrace();
		}

		// ----- END TRANSACTION BOUNDARY -------------------------------------
	}
}
