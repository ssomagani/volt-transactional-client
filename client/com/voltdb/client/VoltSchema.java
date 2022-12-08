package com.voltdb.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.VoltTable.ColumnInfo;
import org.voltdb.client.Client2;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

public class VoltSchema {
	
	private final HashMap<String, ClientVoltTable> tables = new HashMap<>();
	private final HashMap<String, ArrayList<ColumnWithPosition>> procedures = new HashMap<>();
	
	private Client2 client;

	public VoltSchema(Client2 client) throws IOException, ProcCallException {
		this.client = client;
		loadProcedures();
		loadTableColumns();
		loadPrimaryKeys();
		attachProcsToTables();
	}
	
	private void loadTableColumns() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "COLUMNS");
		VoltTable result = resp.getResults()[0];
				
		HashMap<String, ArrayList<ColumnWithPosition>> tableCols = new HashMap<>();
		
		while(result.advanceRow()) {
			String tableName = result.getString(2);
			ArrayList<ColumnWithPosition> columns = tableCols.get(tableName);
			if(columns == null) {
				columns = new ArrayList<ColumnWithPosition>();
				tableCols.put(tableName, columns);
			}
			
			ColumnInfo col = new ColumnInfo(
					result.getString(3), 
					VoltType.typeFromString(result.getString(5))
					);
			
			columns.add(new ColumnWithPosition(result.getLong(16), col));
		}
		
		tableCols.keySet().forEach((tableName) -> {
			ClientVoltTable clientVoltTable = new ClientVoltTable(tableName);
			Collections.sort(tableCols.get(tableName));
			clientVoltTable.createTable(ColumnWithPosition.convertToColumnInfo(tableCols.get(tableName)));
			tables.put(tableName, clientVoltTable);
		});
	}
	
	private void loadPrimaryKeys() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "PRIMARYKEYS");
		VoltTable result = resp.getResults()[0];
		while(result.advanceRow()) {
			String tableName = result.getString(2);
			ClientVoltTable table = tables.get(tableName);
			table.primaryKeyIndices.add(table.getColumnIndex(result.getString(3)));
		}
		
		tables.values().stream().forEach((table) -> {
			Collections.sort(table.primaryKeyIndices);
		});
	}
	
	private void loadProcedures() throws IOException, ProcCallException {
		ClientResponse resp = client.callProcedureSync("@SystemCatalog", "PROCEDURECOLUMNS");
		VoltTable result = resp.getResults()[0];
	
		while(result.advanceRow()) {
			String proc = result.getString(2);
			ArrayList<ColumnWithPosition> columns = procedures.get(proc);
			if(columns == null) {
				columns = new ArrayList<ColumnWithPosition>();
				procedures.put(proc, columns);
			}
			
			String type = result.getString(6);
			if(type.equals("OTHER"))
				type = "VOLTTABLE";
			ColumnInfo col = new ColumnInfo(
					result.getString(3), 
					VoltType.typeFromString(type)
					);
			columns.add(new ColumnWithPosition((int) result.getLong(17), col));
		}
	}
	
	private void attachProcsToTables() {
		tables.keySet().forEach((tableName) -> {
			String insertProc = tableName.toUpperCase() + ".insert";
			if(procedures.containsKey(insertProc)) {
				ArrayList<ColumnWithPosition> sortedProcCols = (ArrayList<ColumnWithPosition>) procedures.get(insertProc).clone();
				Collections.sort(sortedProcCols);
				VoltTable insertColsTable = new VoltTable(ColumnWithPosition.convertToColumnInfo(sortedProcCols));
				tables.get(tableName).setInsertCols(insertColsTable);
			}
			
			String deleteProc = tableName.toUpperCase() + ".delete";
			if(procedures.containsKey(deleteProc)) {
				ArrayList<ColumnWithPosition> sortedProcCols = (ArrayList<ColumnWithPosition>)procedures.get(deleteProc).clone();
				Collections.sort(sortedProcCols);
				VoltTable primaryColsTable = new VoltTable(ColumnWithPosition.convertToColumnInfo(sortedProcCols));
				tables.get(tableName).setPrimaryCols(primaryColsTable);
			}
			
			String updateProc = tableName.toUpperCase() + ".update";
			if(procedures.containsKey(updateProc)) {
				ArrayList<ColumnWithPosition> sortedProcCols = (ArrayList<ColumnWithPosition>)procedures.get(updateProc).clone();
				Collections.sort(sortedProcCols);
				VoltTable updateColsTable = new VoltTable(ColumnWithPosition.convertToColumnInfo(sortedProcCols));
				tables.get(tableName).setUpdateColsTable(updateColsTable);
			}
		});
	}
	
	public ClientVoltTable getTable(String tableName) {
		return tables.get(tableName);
	}
}
