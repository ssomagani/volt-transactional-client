package com.voltdb.clienttxn;

import java.io.IOException;

import org.voltdb.client.Client2;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ProcCallException;

public class TearDown extends Test {

	public static void main(String[] args) throws ProcCallException, ClassNotFoundException, IOException {
		Client2 client = ClientFactory.createClient(new Client2Config());
		client.connectSync("localhost");
		
		client.callProcedureSync("@AdHoc", "delete from undo_log");
		client.callProcedureSync("@AdHoc", "delete from user");
		client.callProcedureSync("@AdHoc", "delete from user_usage");
		client.callProcedureSync("@AdHoc", "delete from product");
		client.callProcedureSync("@AdHoc", "delete from foreign_keys");
	}
}
