package com.voltdb.clienttxn;

import org.voltdb.VoltTable;

import java.util.Arrays;
import java.util.function.Consumer;

import org.voltdb.VoltCompoundProcedure.CompoundProcAbortException;
import org.voltdb.client.ClientResponse;

public class Utils {


	public static VoltTable verifyAndGetTheResults(ClientResponse[] resp) {
		return verifyAndGetTheResults(resp, 0);
	}
	
	public static VoltTable verifyAndGetTheResults(ClientResponse[] resp, int resultIndex) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throw new CompoundProcAbortException(resp[0].getStatusString());
		} else {
			VoltTable results = resp[0].getResults()[resultIndex];
			if(results.advanceRow())
				return results;
		}
		return null;
	}
	
	public static void applyToResults(ClientResponse[] resp, int resultIndex, Consumer<VoltTable> resultFn) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throw new CompoundProcAbortException(resp[0].getStatusString());
		} else {
			VoltTable results = resp[0].getResults()[resultIndex];
			while(results.advanceRow()) {
				resultFn.accept(results);
			}
		}
	}
	
	public static Boolean allSuccessResponses(ClientResponse[] resp) {
		return Arrays.stream(resp).allMatch( 
				(x) -> x.getStatus() == ClientResponse.SUCCESS);
	}
	
	public static void runIfAllSuccessResponses(ClientResponse[] resp, Consumer<VoltTable> resultFn) {
		if(Arrays.stream(resp).allMatch( 
				(x) -> x.getStatus() == ClientResponse.SUCCESS)) {
			Arrays.stream(resp)
			.map( (x) -> x.getResults())
			.forEach( (x) -> resultFn.accept(x[0]));
		}
	}
}
