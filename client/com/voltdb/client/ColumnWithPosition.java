package com.voltdb.client;

import java.util.ArrayList;

import org.voltdb.VoltTable.ColumnInfo;

public class ColumnWithPosition implements Comparable<ColumnWithPosition> {

	public final ColumnInfo column;
	public final int position;
	
	public ColumnWithPosition(int position, ColumnInfo column) {
		this.column = column;
		this.position = position;
	}
	
	public ColumnWithPosition(long position, ColumnInfo column) {
		this.column = column;
		this.position = (int) position;
	}

	public static ColumnInfo[] convertToColumnInfo(ArrayList<ColumnWithPosition> cols) {
		ColumnInfo[] colInfos = new ColumnInfo[cols.size()];
		for(int i=0; i<cols.size(); i++) {
			colInfos[i] = cols.get(i).column;
		}
		return colInfos;
	}

	@Override
	public int compareTo(ColumnWithPosition o) {
		return position - o.position;
	}
}
