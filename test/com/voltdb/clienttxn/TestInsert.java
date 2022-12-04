package com.voltdb.clienttxn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;

import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;
import org.voltdb.types.TimestampType;

import com.voltdb.client.TransactionalClient;

public class TestInsert {

	static TransactionalClient client;

	public static void main(String[] args) throws ProcCallException, ClassNotFoundException {

		Client2Config config = new Client2Config();

		client = new TransactionalClient(config);
		try {
			client.connect("localhost");

						testInsertCommit();
			//			testInsertRollback();
//			test();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void testInsertCommit() throws IOException, ProcCallException {
		try { 

			String txnId = client.startTransaction();
			insert(txnId);
			client.commit();
			verify(txnId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		tearDown();
	}

	private static void testInsertRollback() throws IOException, ProcCallException {
		try { 

			String txnId = client.startTransaction();
			insert(txnId);
			client.rollback();
			verify(txnId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		//		tearDown();
	}

	private static void insert(String txnId) throws IOException, ProcCallException, Exception {
		client.insert(txnId, "USER_USAGE", 1, 2, 3, 200, new TimestampType("2022-12-04 11:07:21.242000"));
	}

	private static void verify(String txnId) throws Exception {
		VoltTable rowValues = new VoltTable(
				new VoltTable.ColumnInfo("id", VoltType.INTEGER)
				);
		Object[] selectVals = {1};
		rowValues.addRow(selectVals);

		ClientResponse resp = client.select("user_select_by_id", rowValues);
		if(resp.getStatus() == ClientResponse.SUCCESS) {
			VoltTable result = resp.getResults()[0];
			if(result.advanceRow())
				assert(result.getString(1) != "Luke") : " Unepected value in row" + result.getString(1);
			else 
				throw new Exception ("Didn't find rows that were meant to be inserted");
		} else {
			throw new Exception (resp.getStatusString());
		}
	}

	private static void tearDown() throws IOException, ProcCallException {
		client.client.callProcedureSync("@AdHoc", "delete from user");
	}

	private static void test() throws IOException, ProcCallException, ClassNotFoundException {
		
		Object[] args = new Object[] {1000000000000000l, "sing", "movie", "musical", new Timestamp(9090909090l)};
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(args);
		
		byte[] ba = bos.toByteArray();
		System.out.println(ba.length);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(ba);
		ObjectInput in = new ObjectInputStream(bis);
		Object[] deserArgs = (Object[]) in.readObject();
		
		Arrays.stream(deserArgs).forEach(x -> System.out.println(x));
	}
}
