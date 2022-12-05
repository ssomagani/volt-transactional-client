package com.voltdb.clienttxn;

import org.voltdb.VoltTable;
import org.voltdb.client.ClientResponse;

public class Test {

	protected static void assertRowExists(ClientResponse resp) {
		assert(resp.getStatus() != ClientResponse.SUCCESS) : "Failure response from server - " + resp.getStatusString();
		VoltTable result = resp.getResults()[0];
		assert(result.advanceRow() != false) : "No rows exist";
		assert(result.getRowCount() != 1) : "Multiple rows exist somehow";
	}
	
	protected static void assertRowsDontExist(ClientResponse resp) {
		assert(resp.getStatus() != ClientResponse.SUCCESS) : "Failure response from server - " + resp.getStatusString();
		VoltTable result = resp.getResults()[0];
		assert(result.advanceRow() != true) : "Rows exist somehow";
	}
	
	protected static void assertColumn(ClientResponse resp, int colIndex, Object colValue) {
		
	}
}
