package com.voltdb.client;

import java.io.IOException;

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
			String clientTxnId = client.startTransaction();
			
			response = client.callProcedureSync(clientTxnId, "test_proc");
			
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
