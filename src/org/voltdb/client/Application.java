package org.voltdb.client;

import java.io.IOException;

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
			String clientTxnId = client.startTransaction();
			int counter = 0;
			
			// business logic
			response = client.callProcedureSync(clientTxnId, ++counter, "StoredProcA");
			response = client.callProcedureSync(clientTxnId, ++counter, "StoredProcB");
			
			if(response.getAppStatus() == 0) {
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
