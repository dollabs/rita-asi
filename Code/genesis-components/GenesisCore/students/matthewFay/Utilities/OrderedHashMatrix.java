package matthewFay.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/***
 * This is a partial implementation to meet pressing needs
 * @author Matthew
 *
 * @param <ROW>
 * @param <COL>
 * @param <DATA>
 */
public class OrderedHashMatrix<ROW, COL, DATA extends Comparable<? super DATA>> extends HashMatrix<ROW, COL, DATA> {
	public List<COL> getOrderedColKeySet(ROW r) {
		List<COL> sorted = new ArrayList<>(this.keySetCols(r));
		
		COL_Comparator<ROW, COL, DATA> comp = new COL_Comparator<>(this, r);
		
		Collections.sort(sorted, comp);
		
		return sorted;
	}
	
	public List<ROW> getOrderedRowKeySet(COL c) {
		List<ROW> sorted = new ArrayList<>(this.keySetRows(c));
		
		ROW_Comparator<ROW, COL, DATA> comp = new ROW_Comparator<>(this, c);
		
		Collections.sort(sorted, comp);
		
		return sorted;
	}
	
	private class COL_Comparator<r, c, d extends Comparable<? super d>> implements Comparator<c>{
		private OrderedHashMatrix<r, c, d> _matrix = null;
		private r _row = null;
		
		public COL_Comparator(OrderedHashMatrix<r, c, d> matrix, r row) {
			_matrix = matrix;
			_row = row;
		}

		@Override
		public int compare(c col1, c col2) {
			// TODO Auto-generated method stub
			d d1 = _matrix.get(_row, col1);
			d d2 = _matrix.get(_row, col2);
			
			return d2.compareTo(d1);
		}
	}
	
	private class ROW_Comparator<r, c, d extends Comparable<? super d>> implements Comparator<r>{
		private OrderedHashMatrix<r, c, d> _matrix = null;
		private c _col = null;
		
		public ROW_Comparator(OrderedHashMatrix<r, c, d> matrix, c col) {
			_matrix = matrix;
			_col = col;
		}

		@Override
		public int compare(r row1, r row2) {
			// TODO Auto-generated method stub
			d d1 = _matrix.get(row1, _col);
			d d2 = _matrix.get(row2, _col);
			
			return d2.compareTo(d1);
		}
	}
}
