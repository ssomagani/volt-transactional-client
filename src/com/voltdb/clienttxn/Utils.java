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
		if(resp.length > 0) {
			if(resp[0].getStatus() != ClientResponse.SUCCESS) {
				throwException(resp[0].getStatusString());
			} else {
				if(resp[0].getResults().length > resultIndex) {
					VoltTable results = resp[0].getResults()[resultIndex];
					if(results.advanceRow())
						return results;
				}
			}
		}
		return null;
	}
	
	public static void applyToAllResponses(ClientResponse[] resp, Consumer<VoltTable> resultFn) {
		Arrays.stream(resp).forEach(x -> {
			if(x.getStatus() != ClientResponse.SUCCESS) {
				throwException(x.getStatusString());
			} else {
				Arrays.stream(x.getResults()).forEach(y -> {
					while(y.advanceRow())
						resultFn.accept(y);
				});
			}
		});
	}
	
	public static void applyToAllResults(ClientResponse[] resp, Consumer<VoltTable> resultFn) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throwException(resp[0].getStatusString());
		} else {
			Arrays.stream(resp[0].getResults()).forEach(x -> {
				while(x.advanceRow()) {
					resultFn.accept(x);
				}
			});
		}
	}
	
	public static void applyToResults(ClientResponse[] resp, int resultIndex, Consumer<VoltTable> resultFn) {
		if(resp[0].getStatus() != ClientResponse.SUCCESS) {
			throwException(resp[0].getStatusString());
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
	
	private static void throwException(String msg) {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		Arrays.stream(ste).forEach(x -> System.out.println(x));
		throw new CompoundProcAbortException(msg);
	}
}
