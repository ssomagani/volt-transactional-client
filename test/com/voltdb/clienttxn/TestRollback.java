package com.voltdb.clienttxn;

import java.io.IOException;
import java.util.Calendar;

import org.voltdb.client.Client2Config;
import org.voltdb.client.ProcCallException;
import org.voltdb.types.TimestampType;

import com.voltdb.client.TransactionalClient;

public class TestRollback extends Test {

	public static void main(String[] args) throws ProcCallException, ClassNotFoundException, IOException {
		
		TearDown.main(null);

		TransactionalClient client = new TransactionalClient(new Client2Config());
		client.connect("localhost");
		
		String txnId = client.startTransaction();
		
		TimestampType now = new TimestampType(Calendar.getInstance().getTime());
		client.insert(txnId, "USER_USAGE", 2, 3, 4, 200, now);
		assertRowExists(client.client.callProcedureSync("undo_log.select", txnId, now));
		
		now = new TimestampType(Calendar.getInstance().getTime());
		client.insert(txnId, "USER", 1, "luke", now);
		assertRowExists(client.client.callProcedureSync("undo_log.select", txnId, now));
		
		client.rollback();
		assertRowsDontExist(client.client.callProcedureSync("@AdHoc", "select * from undo_log where txn_id = '" + txnId + "'"));
		assertRowsDontExist(client.client.callProcedureSync("@AdHoc", "select * from user_usage where user_id = 2"));
		assertRowsDontExist(client.client.callProcedureSync("@AdHoc", "select * from user where id = 1"));
	}
}
