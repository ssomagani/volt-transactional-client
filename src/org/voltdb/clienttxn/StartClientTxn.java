package org.voltdb.clienttxn;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;

public class StartClientTxn extends VoltProcedure {

	private static final SQLStmt INSERT_TXN_ID = new SQLStmt("insert into client_transactions values (?, 0, null)");
	public long run(String uuid) {
		voltQueueSQL(INSERT_TXN_ID, uuid);
		return 0;
	}
}
