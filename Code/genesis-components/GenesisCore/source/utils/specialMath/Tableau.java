package utils.specialMath;
import java.util.ArrayList;
import java.util.List;
public class Tableau {
	private static final double EPSILON = 0.000000001;
	private final class Cell {
		Double dval;
		public boolean starred = false;
		public boolean primed  = false;
		public Cell(double d){
			dval = d;
		}
		public Cell(Double d){
			dval = d;
		}
		public Cell(){
			dval = 0.0;
		}
		public Cell(Cell c){
			starred = c.starred;
			primed  = c.primed;
			dval = c.dval;
		}
		public String toString(){
			String s = String.format("%2.2f", dval.doubleValue());
			if( starred) s+= "*";
			if(primed) s+= "'";
			return s;
		}
		public double doubleValue(){
			return dval.doubleValue();
		}
		
	}
	
	private List<Boolean> markedRows;
	private List<Boolean> markedCols;
	private List<List<Cell>> cList;
	private int rowLength;
	private int colLength;
	public Tableau clone(){
		Tableau t = new Tableau(colLength,rowLength);
		t.markedCols = new ArrayList<Boolean>(markedCols);
		t.markedRows = new ArrayList<Boolean>(markedRows);
		t.cList = new ArrayList<List<Cell>>(colLength);
		for(int i=0;i<colLength;i++){
			List<Cell> cd = new ArrayList<Cell>(rowLength);
			for(int j=0;j<rowLength;j++){
				cd.add(getCell(i,j));
			}
			t.cList.add(cd);
		}
		return t;
	}
	public Tableau(int numRows, int numCols){
		rowLength=numCols;
		colLength=numRows;
		cList = new ArrayList<List<Cell>>(numRows);
		for(int i=0;i<numRows;i++){
			List<Cell> cd = new ArrayList<Cell>(numCols);
			for(int j=0;j<rowLength;j++){
				cd.add(new Cell(0.0));
			}
			cList.add(cd);
		}
		markedCols = new ArrayList<Boolean>(numCols);
		markedRows = new ArrayList<Boolean>(numRows);
		for(int i=0;i<numRows;i++){
			markedRows.add(false);
		}
		for (int i=0;i<numCols;i++){
			markedCols.add(false);
		}
	}
	
	//Row Ops
	public boolean rowHasStarredCell(int rowNum){
		List<Cell> row = getRowCells(rowNum);
		for(Cell c:row){
			if(c.starred) return true;
		}
		return false;
	}
	public int getRowLength(){
		return rowLength;
	}
	public void markRow(int rowNum){
		assert(rowNum>=0 && rowNum < colLength);
		markedRows.set(rowNum, true);
	}
	public void unmarkRow(int rowNum){
		assert(rowNum>=0 && rowNum < colLength);
		markedRows.set(rowNum, false);
	}
	public void unmarkAllRows(){
		for(int i=0;i<colLength;i++){
			markedRows.set(i,false);
		}
	}
	public boolean rowIsMarked(int rowNum){
		assert(rowNum>= 0 && rowNum < colLength);
		return markedRows.get(rowNum);
	}
	private List<Cell> getRowCells(int rowNum){
		assert( rowNum >= 0 && rowNum < colLength);
		return new ArrayList<Cell>(cList.get(rowNum));
	}
	
	//Column Ops
	public boolean colHasStarredCell(int colNum){
		List<Cell> col = getColCells(colNum);
		for(Cell c:col){
			if(c.starred) return true;
		}
		return false;
	}
	public int getColLength(){
		return colLength;
	}
	public void markCol(int colNum){
		assert(colNum>=0 && colNum < rowLength);
		markedCols.set(colNum, true);
	}
	public void unmarkCol(int colNum){
		assert(colNum>=0 && colNum < rowLength);
		markedCols.set(colNum, false);
	}
	public void unmarkAllCols(){
		for(int i=0;i<rowLength;i++){
			markedCols.set(i,false);
		}
	}
	public boolean colIsMarked(int colNum){
		assert(colNum >=0 && colNum < rowLength);
		return markedCols.get(colNum);
	}
	public List<Cell> getColCells(int colNum){
		assert(colNum >= 0 && colNum < rowLength);
		List<Cell> ret = new ArrayList<Cell>(colLength);
		for(int i=0;i<colLength;i++){
			ret.add(cList.get(i).get(colNum));
		}
		return ret;
	}
	
	
	//Tableau Ops
	public void clearAllMarks(){
		unmarkAllCols();
		unmarkAllRows();
	}
	public String toString(){
		StringBuffer s = new StringBuffer(5*rowLength*colLength);
		s.append("Tableau:\n");
		for(int i = 0;i<rowLength;i++){
			if(colIsMarked(i)) s.append("c\t");
			else s.append("\t");
		}
		s.append("\n");
		for(int i=0;i<colLength;i++){
			for( Cell d: getRowCells(i)){
				s.append(d+"\t");
			}
			s.delete(s.lastIndexOf("\t"),s.lastIndexOf("\t")+1);
			if(rowIsMarked(i)) s.append(" c");
			else s.append("\t");
			s.append("\n");
		}
		return s.toString();
	}
	public void set(int rowNum, int colNum, double val){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		if(Double.valueOf(val).equals(Double.NaN) || Double.valueOf(val).equals(Double.NEGATIVE_INFINITY) || Double.valueOf(val).equals(Double.POSITIVE_INFINITY)){
			throw new IllegalArgumentException("Somebody tried to put the value "+val+" into a Hungarian tableau!");
		}
		boolean starred = getCell(rowNum,colNum).starred;
		boolean primed  = getCell(rowNum,colNum).primed;
		cList.get(rowNum).set(colNum, new Cell( val));
		getCell(rowNum,colNum).primed = primed;
		getCell(rowNum,colNum).starred= starred;
		
	}
	public void set(int rowNum, int colNum, Cell val){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		cList.get(rowNum).set(colNum, new Cell( val));
	}
	public void star(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		assert(get(rowNum,colNum)<EPSILON);
		cList.get(rowNum).get(colNum).starred = true;
		cList.get(rowNum).get(colNum).primed = false;
	}
	public void prime(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		assert(get(rowNum,colNum)<EPSILON);
		cList.get(rowNum).get(colNum).primed = true;
		cList.get(rowNum).get(colNum).starred = false;
	}
	public void unstar(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		assert(get(rowNum,colNum)<EPSILON);
		cList.get(rowNum).get(colNum).starred = false;
	}
	public void unprime(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
//		assert(get(rowNum,colNum)<EPSILON);
		cList.get(rowNum).get(colNum).primed = false;
	}
	public boolean isPrimed(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		return cList.get(rowNum).get(colNum).primed;
	}
	public boolean isStarred(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		return cList.get(rowNum).get(colNum).starred;
	}
	public double get(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		return cList.get(rowNum).get(colNum).doubleValue();
	}
	public Cell getCell(int rowNum, int colNum){
		assert(0 <= rowNum && 0 <= colNum && rowNum < colLength && colNum < rowLength);
		return cList.get(rowNum).get(colNum);
	}
	
	public static void main(String args[]){
		Tableau t = new Tableau(3,4);
		t.markRow(1);
		t.markCol(1);
		t.markCol(3);
		t.star(1, 1);
		t.prime(2,2);
		t.set(1, 3, 42);
		System.out.println(t);
		
		System.out.println(t.clone());
	}
	
}
