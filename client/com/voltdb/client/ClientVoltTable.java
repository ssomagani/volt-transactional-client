package com.voltdb.client;

import java.util.ArrayList;

import org.voltdb.VoltTable;
import org.voltdb.VoltTable.ColumnInfo;

public class ClientVoltTable {

	public static final ColumnInfo[] TYPE_CONVERSION_PARAM = new ColumnInfo[0];

	public VoltTable _table;
	public final String name;
	public ArrayList<Integer> primaryKeyIndices = new ArrayList<>();
	public int partitionColIndex;
//	private final String insertProc, deleteProc;
	private VoltTable insertColsTable;
	private VoltTable primaryColsTable;
	private VoltTable updateColsTable;

	public ClientVoltTable(String name) {
		this.name = name;
//		this.insertProc = name + ".insert";
//		this.deleteProc = name + ".delete";
	}

	public void setPartitionColIndex(int partitionColIndex) {
		this.partitionColIndex = partitionColIndex;
	}

	public void createTable(ColumnInfo[] colInfo) {
		_table = new VoltTable(colInfo);
	}

	public void createTable(ArrayList<ColumnInfo> columns) {
		_table = new VoltTable(columns.toArray(TYPE_CONVERSION_PARAM));
	}
	
	public void setInsertCols(VoltTable insertColsTable) {
		this.insertColsTable = insertColsTable;
	}
	
	public void setPrimaryCols(VoltTable primaryColsTable) {
		this.primaryColsTable = primaryColsTable;
	}
	
	public void setUpdateColsTable(VoltTable updateColsTable) {
		this.updateColsTable = updateColsTable;
	}
	
	public VoltTable getUpdateColsTable() {
		return updateColsTable;
	}
	
	public VoltTable getInsertArgsTable(Object[] args) {
		VoltTable insertCols = insertColsTable.clone(0);
		insertCols.addRow(args);
		return insertCols;
	}
	
	public int getColumnIndex(String columnName) {
		return _table.getColumnIndex(columnName);
	}

	public VoltTable getLoadedPrimaryKeyTable(VoltTable superSetTable) throws RuntimeException {
		if(!superSetTable.advanceRow())
			throw new RuntimeException("No rows to copy");

		Object[] primaryKeyVals = new Object[primaryKeyIndices.size()];
		
		for(int i = 0; i<primaryKeyIndices.size(); i++) {
			primaryKeyVals[i] = superSetTable.get(primaryKeyIndices.get(i));
		}
		
		VoltTable table = primaryColsTable.clone(0);
		table.addRow(primaryKeyVals);
		return table;
	}
	
	public VoltTable getPrimaryKeyTable() {
		return primaryColsTable.clone(0);
	}

	public VoltTable getUpdateArgsTable(Object[] insertArgs) {
		Object[] updateArgs = new Object[insertArgs.length + primaryKeyIndices.size()];
		System.arraycopy(insertArgs, 0, updateArgs, 0, insertArgs.length);
		for(int i = 0; i<primaryKeyIndices.size(); i++) {
			updateArgs[insertArgs.length + i] = insertArgs[primaryKeyIndices.get(i)];
		}
		VoltTable table = updateColsTable.clone(0);
		table.addRow(updateArgs);
		return table;
	}
}
