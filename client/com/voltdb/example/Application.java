package com.voltdb.example;

import java.io.IOException;

import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import com.voltdb.client.TransactionalClient;

public class Application {

	public static void main(String[] args) throws IOException, ProcCallException {
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
			Object[] values = {"uno", 1};
			procArgs.addRow(values);
			
			response = client.callUpdateProc(txnId, "test_1", 
					"test_1_proc", getUndoValsProcArgs, procArgs);
			
			response = client.callProcedureSync(txnId, "insert_undo_test_2", "test_2_select", 
					"test_2_proc", "test_2_proc", getUndoValsProcArgs, procArgs);
			
			response = client.callSelect("test_select", getUndoValsProcArgs);
			
			VoltTable[] results = response.getResults();
			if(results[0].advanceRow()) {
				String name = results[0].getString("name");
				long id = results[0].getLong("id");
				System.out.println(id + " : " + name);
			}
			
			if(response.getStatus() == ClientResponse.SUCCESS) {
				client.commit();
			} else {
				client.rollback();
			}
		} catch (IOException | ProcCallException e1) {
			client.rollback();
			e1.printStackTrace();
		}

		// ----- END TRANSACTION BOUNDARY -------------------------------------
	}
}
