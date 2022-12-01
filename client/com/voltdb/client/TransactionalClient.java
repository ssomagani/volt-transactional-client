package com.voltdb.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.voltdb.VoltTable;
import org.voltdb.VoltTable.ColumnInfo;
import org.voltdb.VoltType;
import org.voltdb.client.Client2;
import org.voltdb.client.Client2Config;
import org.voltdb.client.ClientFactory;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class TransactionalClient {
	
	public final Client2 client;
	private String txnId;
	private final HashMap<String, ClientVoltTable> tables = new HashMap<>();
	private final HashMap<String, ArrayList<ColumnInfo>> procCols = new HashMap<>();
	
	public TransactionalClient(Client2Config config) {
		client = ClientFactory.createClient(config);
	}
	
	public void connect(String serverString) throws IOException, ProcCallException {
		client.connectSync(serverString);
		getSchema();
	}
	
	private void getSchema() throws IOException, ProcCallException {
		getTableColumns();
		getPrimaryKeys();
		getProcedures();
	}
	
	private void getTableColumns() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "COLUMNS");
		VoltTable result = resp.getResults()[0];
				
		HashMap<String, ArrayList<ColumnInfo>> tableCols = new HashMap<>();
		
		while(result.advanceRow()) {
			String tableName = result.getString(2);
			ArrayList<ColumnInfo> columns = tableCols.get(tableName);
			if(columns == null) {
				columns = new ArrayList<ColumnInfo>();
				tableCols.put(tableName, columns);
			}
			
			ColumnInfo col = new ColumnInfo(
					result.getString(3), 
					VoltType.typeFromString(result.getString(5))
					);
			
			columns.add(col);
		}
		
		tableCols.keySet().forEach((tableName) -> {
			ClientVoltTable clientVoltTable = new ClientVoltTable(tableName);
			clientVoltTable.createTable(tableCols.get(tableName));
			tables.put(tableName, clientVoltTable);
		});
	}
	
	private void getPrimaryKeys() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "PRIMARYKEYS");
		VoltTable result = resp.getResults()[0];
		
		while(result.advanceRow()) {
			String tableName = result.getString(2);
			ClientVoltTable table = tables.get(tableName);
			table.primaryKeyIndices.add(table._table.getColumnIndex(result.getString(3)));
		}
	}
	
	private void getProcedures() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "PROCEDURECOLUMNS");
		VoltTable result = resp.getResults()[0];
	
		while(result.advanceRow()) {
			String proc = result.getString(2);
			ArrayList<ColumnInfo> columns = procCols.get(proc);
			if(columns == null) {
				columns = new ArrayList<ColumnInfo>();
				procCols.put(proc, columns);
			}
			
			String type = result.getString(6);
			if(type.equals("OTHER"))
				type = "VOLTTABLE";
			ColumnInfo col = new ColumnInfo(
					result.getString(3), 
					VoltType.typeFromString(type)
					);
			columns.add(col);
		}
	}

	public String startTransaction() throws IOException, ProcCallException {
		UUID uuid = UUID.randomUUID();
		txnId = uuid.toString();
		return txnId;
	}
	
	public ClientResponse select(String selectProc, VoltTable procArgs) throws IOException, ProcCallException {
		if(procArgs.advanceRow())
			return client.callProcedureSync(selectProc, procArgs.getRowObjects());
		return null;
	}
	
	public ClientResponse update(
			String txnId,
			String table,
			String updateProc,
			VoltTable whereClauseArgs,
			VoltTable procArgs
			) throws IOException, ProcCallException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = "insert_undo_" + table;
		allArgs[2] = table + "_select_by_id";
		allArgs[3] = updateProc;
		allArgs[4] = updateProc;
		allArgs[5] = whereClauseArgs;
		allArgs[6] = procArgs;
		ClientResponse resp = client.callProcedureSync("RollbackableTxn", allArgs);
		return resp;
	}
	
	public ClientResponse insert(
			String txnId,
			String table,
			VoltTable procArgs
			) throws IOException, ProcCallException {
		Object[] allArgs = new Object[5];
		allArgs[0] = txnId;
		allArgs[1] = table + "_undo_insert_blank";
		allArgs[2] = table + "_delete";
		allArgs[3] = table + "_insert";
		allArgs[4] = procArgs;
		return client.callProcedureSync("Insert", allArgs);
	}
	
	public ClientResponse delete(
		String txnId,
		String table,
		VoltTable whereClauseArgs,
		VoltTable procArgs) throws IOException, ProcCallException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = table + "_select_by_id";
		allArgs[2] = "insert_undo_" + table;
		allArgs[3] = "insert_" + table;
		allArgs[4] = "delete_" + table;
		allArgs[5] = whereClauseArgs;
		allArgs[6] = procArgs;
		return client.callProcedureSync("Delete", allArgs);
	}
	
	public ClientResponse callProcedureSync(
			String txnId, 
			String getUndoValsProc, 
			String insertUndoLogProc, 
			String undoStoredProc, 
			String storedProc, 
			VoltTable getUndoValsProcArgs,
			VoltTable procArgs) 
			throws ProcCallException, IOException {
		Object[] allArgs = new Object[7];
		allArgs[0] = txnId;
		allArgs[1] = insertUndoLogProc;
		allArgs[2] = getUndoValsProc;
		allArgs[3] = undoStoredProc;
		allArgs[4] = storedProc;
		allArgs[5] = getUndoValsProcArgs;
		allArgs[6] = procArgs;
		ClientResponse resp = client.callProcedureSync("RollbackableTxn", allArgs);
		return resp;
	}
	
	public void rollback() throws IOException, ProcCallException {
		client.callProcedureSync("Rollback", txnId);
	}
	
	public void commit() throws IOException, ProcCallException {
		client.callProcedureSync("Commit", txnId);
	}
}
