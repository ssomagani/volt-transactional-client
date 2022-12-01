package com.voltdb.client;

import java.util.ArrayList;

import org.voltdb.VoltTable;

public class ClientVoltTable {

	public static final VoltTable.ColumnInfo[] TYPE_CONVERSION_PARAM = new VoltTable.ColumnInfo[0];

	public VoltTable _table;
	public final String name;
	public ArrayList<Integer> primaryKeyIndices = new ArrayList<>();
	public int partitionColIndex;
	
	public ClientVoltTable(String name) {
		this.name = name;
	}
	
	public void setPartitionColIndex(int partitionColIndex) {
		this.partitionColIndex = partitionColIndex;
	}
	
	public void createTable(VoltTable.ColumnInfo[] colInfo) {
		_table = new VoltTable(colInfo);
	}
	
	public void createTable(ArrayList<VoltTable.ColumnInfo> columns) {
		_table = new VoltTable(columns.toArray(TYPE_CONVERSION_PARAM));
	}
}
