package matthewFay.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import utils.Mark;

public class HashMatrix<ROW, COL, DATA> {
	private LinkedHashMap<ROW, LinkedHashMap<COL, DATA>> hashMatrixRowCol;
	private LinkedHashMap<COL, LinkedHashMap<ROW, DATA>> hashMatrixColRow;
	
	public HashMatrix() {
		hashMatrixRowCol = new LinkedHashMap<ROW, LinkedHashMap<COL,DATA>>();
		hashMatrixColRow = new LinkedHashMap<COL, LinkedHashMap<ROW,DATA>>();
	}
	
	public HashMatrix(HashMatrix<ROW, COL, DATA> m) {
		hashMatrixRowCol = new LinkedHashMap<ROW, LinkedHashMap<COL,DATA>>();
		hashMatrixColRow = new LinkedHashMap<COL, LinkedHashMap<ROW,DATA>>();
		
		for(ROW r : m.keySetRows()) {
			for(COL c : m.keySetCols()) {
				if(m.contains(r, c)) {
					this.put(r, c, m.get(r, c));
				}
			}
		}
	}
	
	public Set<ROW> keySetRows() {
		return hashMatrixRowCol.keySet();
	}
	
	public Set<ROW> keySetRows(COL c) {
		if(hashMatrixColRow.containsKey(c))
			return hashMatrixColRow.get(c).keySet();
		return new HashSet<>();
	}
	
	public Set<COL> keySetCols() {
		return hashMatrixColRow.keySet();
	}
	
	public Set<COL> keySetCols(ROW r) {
		if(hashMatrixRowCol.containsKey(r))
			return hashMatrixRowCol.get(r).keySet();
		return new HashSet<>();
	}
	
	public Set<DATA> getValues() {
		Set<DATA> data = new HashSet<DATA>();
		for(ROW r : hashMatrixRowCol.keySet()) {
			for(COL c : hashMatrixRowCol.get(r).keySet()) {
				data.add(hashMatrixRowCol.get(r).get(c));
			}
		}
		return data;
	}
	
	public void put(ROW x, COL y, DATA d) {
		if(!hashMatrixRowCol.containsKey(x)) {
			hashMatrixRowCol.put(x, new LinkedHashMap<COL, DATA>());
		}
		if(hashMatrixRowCol.get(x).containsKey(y)) {
			hashMatrixRowCol.get(x).remove(y);
		}
		hashMatrixRowCol.get(x).put(y, d);
		
		if(!hashMatrixColRow.containsKey(y)) {
			hashMatrixColRow.put(y, new LinkedHashMap<ROW, DATA>());
		}
		if(hashMatrixColRow.get(y).containsKey(x)) {
			hashMatrixColRow.get(y).remove(x);
		}

		hashMatrixColRow.get(y).put(x, d);
	}
	
	public void remove(ROW x, COL y) {
		if(hashMatrixRowCol.containsKey(x)) {
			hashMatrixRowCol.get(x).remove(y);
			if(hashMatrixRowCol.get(x).size() == 0)
				hashMatrixRowCol.remove(x);
		}
		if(hashMatrixColRow.containsKey(y)) {
			hashMatrixColRow.get(y).remove(x);
			if(hashMatrixColRow.get(y).size() == 0)
				hashMatrixColRow.remove(y);
		}
	}
	
	public boolean containsKey(ROW x, COL y) {
		return contains(x,y);
	}
	
	public boolean contains(ROW x, COL y) {
		if (hashMatrixRowCol.containsKey(x)) {
			if(hashMatrixRowCol.get(x).containsKey(y))
				return true;
		}
		return false;
	}
	
	public DATA get(ROW x, COL y) {
		if(!hashMatrixRowCol.containsKey(x))
			return null;
		if(!hashMatrixRowCol.get(x).containsKey(y))
			return null;
		return hashMatrixRowCol.get(x).get(y);
	}

	public void clear() {
		hashMatrixRowCol.clear();
		hashMatrixColRow.clear();
	}
	
	public String toCSV() {
		String s = "";
		ArrayList<ROW> rows = new ArrayList<ROW>();
		ArrayList<COL> cols = new ArrayList<COL>();
		for(ROW row : hashMatrixRowCol.keySet())
			rows.add(row);
		for(COL col : hashMatrixColRow.keySet())
			cols.add(col);
		for(ROW row : rows) {
			if(!s.isEmpty())
				s = s+"\n";
			String line = "";
			for(COL col : cols) {
				if(!line.isEmpty())
					line = line + ",";
				line = line + this.get(row, col);
			}
			s = s + line;
		}
		return s;
	}
	
	public static void main(String args[]) {
		HashMatrix<String, String, Float> h = new HashMatrix<String, String, Float>();
		
		h.put("1", "1", 2.0f);
		h.put("1.5","1", 2.5f);
		h.put("1","2",3f);
		
		Mark.say(h.toCSV());
		
		
	}
}